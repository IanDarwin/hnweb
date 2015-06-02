package data;

import java.io.Serializable;
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

import org.omg.CORBA.StringHolder;

/**
 * Implements Gateway, an Adam Bien pattern whose purpose is to expose
 * an Entity (and its relations) to the Client/Web tier, rather like a Seam2 "Home Object"
 * @author Ian Darwin
 */
@Stateful @Named @ConversationScoped
public class PersonHome implements Serializable {
	
	@Inject private PersonList lister;
	ArrayList<Member> members;

	private static final long serialVersionUID = -2284578724132631798L;

	private static final String LIST_PAGE = "PersonList";
	private static final String FORCE_REDIRECT = "?faces-redirect=true";

	@Inject Conversation conv;

	// Must be Long (not long) so we can check for null
	private Long id;
	private Member instance = new Member();
	private boolean enableMasterMember;

	@PersistenceContext(type=PersistenceContextType.EXTENDED) EntityManager em;


	public PersonHome() {
		// System.out.println("PersonHome.PersonHome()");
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
			System.out.println("PersonHome.getMemberList() CACHE HIT");
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
			throw new IllegalArgumentException("Person not found by id! " + id);
		}
	}
	public void wire(Long id) {
		setId(id);
		wire();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String update() {
		em.merge(instance);
		conv.end();
		invalidateMemberList();
		return LIST_PAGE + FORCE_REDIRECT;
	}
	
	public void create() {
		// Nothing to do, instance is pre-created
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String save() {
		em.persist(instance);
		conv.end();
		invalidateMemberList();
		return LIST_PAGE + FORCE_REDIRECT;
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

	public void handleTypeChange() {
		// For AJAX
		enableMasterMember = instance.getMemberType() == MemberType.Secondary;
	}
	
	public boolean isEnableMasterMember() {
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
		System.out.println("PersonHome.bfn()");
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
		valid = validatePostCode(country, postCode, messageHolder);
		if (!valid) {
			jsfContext.addMessage(
					toValidate.getClientId(jsfContext), 
					new FacesMessage(messageHolder.value));
	        ((UIInput) toValidate).setValid(false);
	        return;
		}
	}

	/** 
	 * Method is NOT API and is only exposed for testing
	 * @param country The country
	 * @param postCode The post code
	 * @param invalidMessage A holder for a message if invalid
	 * @return The syntactic validity of the given code
	 */
	boolean validatePostCode(Country country, String postCode, StringHolder invalidMessage) {
		boolean valid = true;
		switch(country) {
		case CA:
			valid = postCode.matches("\\w\\d\\w\\s?\\d\\w\\d");
			invalidMessage.value = "PostCode must match ANA NAN pattern";
			break;
		case US:
			valid = postCode.matches("\\d{5}(-\\d{4})?");
			invalidMessage.value = "Zip Code must be 5 digits, optional -4 digits";
		case ZZ:
			break;
		default:
			throw new IllegalStateException("Unknown country");
		}
		return valid;
	}
	
	public void validateEmail(FacesContext jsfContext,
            UIComponent toValidate, 
            Object userInputValue) {
		
		boolean valid = true;
		String email = (String) userInputValue;
		StringHolder messageHolder = new StringHolder();
		valid = validateEmail(email, messageHolder);
		if (!valid) {
			jsfContext.addMessage(
					toValidate.getClientId(jsfContext), 
					new FacesMessage(messageHolder.value));
	        ((UIInput) toValidate).setValid(false);
	        return;
		}
	}
	
	/** As above, method is exposed only to facilitate testing
	 * @param email The email address to be validated
	 * @param messageHolder The response holder
	 * @return True if the message is not invalid
	 */
	static boolean validateEmail(String email, StringHolder messageHolder) {
		boolean valid = true;
		if (email == null || email.length() == 0) 
			return valid;
		messageHolder.value = "Invalid email syntax, I think";
		return email.matches("[\\w.]+@[\\w.]+\\.\\w+");
	}
}
