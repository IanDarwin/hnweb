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

import model.Country;
import model.Member;
import model.MemberType;
import model.Transaction;
import model.TransactionReason;

import org.omg.CORBA.StringHolder;

/**
 * Implements Gateway, an Adam Bien pattern whose purpose is to expose
 * an Entity (and its relations) to the Client/Web tier, rather like a Seam2 "Home Object"
 * @author Ian Darwin
 */
@Stateful @Named @ConversationScoped
public class MemberHome implements Serializable {
	
	@Inject private MemberList lister;
	ArrayList<Member> members;

	private static final long serialVersionUID = -2284578724132631798L;

	private static final String LIST_PAGE = "MemberList";
	private static final String FORCE_REDIRECT = "?faces-redirect=true";

	@Inject Conversation conv;

	// Must be Long (not long) so we can check for null
	private Long id;
	private Member instance = new Member();
	private boolean enableMasterMember;

	@PersistenceContext(type=PersistenceContextType.EXTENDED) EntityManager em;


	public MemberHome() {
		// System.out.println("MemberHome.MemberHome()");
	}

	Member noMember = new Member("--", "Select a member", "---", null);
	
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
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void wire() {
		if (conv.isTransient()) {
			conv.begin();
		}
		if (id == null) {
			instance = new Member();
			return;
		}
		instance = em.find(Member.class, id);
		if (instance == null) {
			throw new IllegalArgumentException("Member not found by id! " + id);
		}
	}
	public void wire(Long id) {
		setId(id);
		wire();
	}
	
	public void create() {
		// Nothing to do, instance is pre-created
	}
	
	public String saveOrUpdate() {
		if (id == null) {
			return save();
		} else {
			return update();
		}
	}

	/** Save a new member */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String save() {
		instance.setExpiryYear(getNewMemberExpiryYear(LocalDate.now()));
		em.persist(instance);
		Transaction tx = new Transaction(instance, 1, TransactionReason.N);
		em.persist(tx);
		conv.end();
		invalidateMemberList();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	/** Update an existing member */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String update() {
		em.merge(instance); // in case detached - shouldn't be!
		// We don't do a Transaction here since RenewAction will do so if needed.
		conv.end();
		invalidateMemberList();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	/** 
	 * This should be externalized as it depends on our club's policies:
	 * If you sign up on or after September 1 you get next year's membership
	 */
	protected static int getNewMemberExpiryYear(LocalDate now) {
		 return now.getMonthValue()>=9 ? now.getYear() + 1 : now.getYear();
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

	@Resource EJBContext context;

	/** Close an editing operation: mark tx for
	 * rollback, end conversation, 
	 * return List page.
	 * @return The List Page
	 */
	public String cancel() {
		context.setRollbackOnly();
		conv.end();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	/** Like Cancel but for e.g., View page, no conv end.
	 * @return The List Page
	 */
	public String done() {
		return LIST_PAGE;
	}

	public String remove() {
		em.remove(instance);
		conv.end();
		invalidateMemberList();
		return LIST_PAGE;
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
