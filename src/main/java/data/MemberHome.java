package data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.omg.CORBA.StringHolder;

import model.Country;
import model.Member;
import model.MemberTransaction;
import model.MemberType;
import model.TransactionReason;
import webtier.WebConstants;

/**
 * Implements Gateway, an Adam Bien pattern whose purpose is to expose
 * an Entity (and its relations) to the Client/Web tier, rather like a Seam2 "Home Object"
 * @author Ian Darwin
 */
@Stateful @Named @ConversationScoped
public class MemberHome implements Serializable {
	
	private static final long serialVersionUID = -2284578724132631798L;

	@PersistenceContext(type=PersistenceContextType.EXTENDED) protected EntityManager em;

	private static final String LIST_PAGE = "MemberList";

	@Inject Conversation conv;

	@Inject private MemberList lister;
	List<Member> members;

	// Must be Long (not long) so we can check for null
	private Long id;

	protected Member instance = new Member();
	// Used to enable the "Master Member" UI control while editing a Secondary Member
	private boolean enableMasterMember;
	// For the benefit of the UI controls only
	final static Member noMember = new Member("--", "Select a member", "---", null);

	public MemberHome() {
		System.out.println("MemberHome.MemberHome()");
	}

	public void newInstance() {
		instance = new Member();
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Cache this list to avoid too many re-creations.
	 * @return
	 */
	public List<Member> getMemberList() {
		if (members == null) {
			members = new ArrayList<>();
			members.add(noMember);
			members.addAll(lister.findAll());
		} else {
			System.out.println("MemberHome.getMemberList() CACHE HIT");
		}
		return members;
	}
	/**
	 * But not too many! Has to be re-created after we update,
	 * as it is disconnected copies and is now out of sync.
	 */
	private void invalidateMemberList() {
		members = null;
	}
	
	public void create() {
		// Nothing to do, instance is pre-created
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void wire() {
		if (conv.isTransient()) {
			conv.begin();
		}
		if (id == null || id == 0L) {
			instance = new Member();
			return;
		}
		instance = em.find(Member.class, id);
		if (instance == null) {
			throw new IllegalArgumentException("Member not found by id! " + id);
		}
	}

    // Calls the version that is @Transactional
	public void wire(String id) {
		if (id != null && !id.isEmpty()) {
			setId(Long.parseLong(id));
		}
		wire();
	}

	public String saveOrUpdate() {
		if (id == null) {
			return save();
		} else {
			return update();
		}
	}

	/** The C of CRUD - create a new Member in the database
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String save() {
		instance.setExpiryYear(getNewMemberExpiryYear(LocalDate.now()));
		em.persist(instance);
		MemberTransaction tx = new MemberTransaction(instance, 1, TransactionReason.N);
		// XXX em.persist(tx);
		instance.getMemberTransactions().add(tx);
		conv.end();
		invalidateMemberList();
		return getListPage() + "#p" + instance.getId() + WebConstants.FORCE_REDIRECT;
	}
	
	/** Update an existing member */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String update() {
		em.merge(instance); // in case detached - shouldn't be!
		// We don't do a Transaction here since RenewAction will do so if needed.
		conv.end();
		invalidateMemberList();
		return getListPage() + "#p" + instance.getId() + WebConstants.FORCE_REDIRECT;
	}
	
	/**
	 * This should be externalized as it depends on our club's policies:
	 * If you sign up on or after September 1 you get next year's membership
	 */
	protected static int getNewMemberExpiryYear(LocalDate now) {
		 return now.getMonthValue()>=9 ? now.getYear() + 1 : now.getYear();
	}

	public Member getInstance() {
		return instance;
	}
	public void setInstance(Member instance) {
		this.instance = instance;
		// this.id = instance.getId();
	}

	public void computeEnableMasterMember() {
		// For AJAX
		enableMasterMember = instance.getMemberType() == MemberType.Secondary;
	}
	
	public boolean isEnableMasterMember() {
		computeEnableMasterMember();
		return enableMasterMember;
	}

	public static String getListPage() {
		return LIST_PAGE;
	}

	@Resource EJBContext context;

	/** Close an editing operation: mark tx for
	 * rollback, end conversation,
	 * return List page.
	 * @return The List Page
	 */
	public String cancel() {
		context.setRollbackOnly();
		conv.end();
		return getListPage() + WebConstants.FORCE_REDIRECT;
	}
	
	/** Like Cancel but for e.g., View page, no conv end.
	 * @return The List Page
	 */
	public String done() {
		return getListPage();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String remove() {

		// Use raw sql to archive, via JPA Native Query mechanism
		String archive_sql = "insert into member_archived select * from member where id = ?1";
		Query q = em.createNativeQuery(archive_sql);
		q.setParameter(1, instance.getId());
		int rowCount = q.executeUpdate();
		if (rowCount != 1) {
			System.err.println("WARNING: archive did not succeed");
			return ""; // Stay on page
		}
		em.remove(instance);
		conv.end();
		invalidateMemberList();
		return getListPage();
	}

	@PreDestroy
	public void bfn() {
		System.out.println("MemberHome.bfn()");
	}

	/**
	 * Simple validation of a method in a "home"-type object
	 * that can validate based on related state
	 */
	public void validatePostCode(FacesContext jsfContext,
            UIComponent toValidate,
            Object userInputValue) {
		
		boolean valid = true;
		String postCode = (String) userInputValue;
		Country country = instance.getCountry();
		if (country == null) {
			country = Country.CA;
		}
		StringHolder messageHolder = new StringHolder();
		valid = MemberValidator.validatePostCode(country, postCode, messageHolder);
		if (!valid) {
			jsfContext.addMessage(
					toValidate.getClientId(jsfContext),
					new FacesMessage(messageHolder.value));
	        ((UIInput) toValidate).setValid(false);
	        return;
		}
	}

	public void validateEmail(FacesContext jsfContext,
            UIComponent toValidate,
            Object userInputValue) {
		
		boolean valid = true;
		String email = (String) userInputValue;
		StringHolder messageHolder = new StringHolder();
		valid = MemberValidator.validateEmail(email, messageHolder);
		if (!valid) {
			jsfContext.addMessage(
					toValidate.getClientId(jsfContext),
					new FacesMessage(messageHolder.value));
	        ((UIInput) toValidate).setValid(false);
	        return;
		}
	}
}
