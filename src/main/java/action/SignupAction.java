package action;

import java.io.Serializable;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.inject.Named;
import model.*;
import webtier.WebConstants;

/**
 * Backs the SignupForm page, it holds the signup form's
 * fields (member name, type, email, phone#s, ...) between 
 * the setter calls and * the call to {@link #signup()}.
 */
@Named
@RequestScoped
public class SignupAction implements Serializable {

	private static final long serialVersionUID = 332428792829209817L;

	private static final Logger logger = Logger.getLogger(SignupAction.class.getName());

	private static final String VIEW_PAGE = "/";

	private Member member;
	private String firstName;
	private String lastName;
	private String city;
	private String email;

	private String homePhone;
	private String cellPhone;
	private String workPhone;

	@PersistenceContext
	transient EntityManager em;

	/**
	 * Tada!! Create a new member in our database.
	 * @return The outcome: the member's page on success; null (stay here) on error.
	 */
	@Transactional
	public String signup1() {
		Member member = new Member(firstName, null, lastName, email);
		member.setHomePhone(getHomePhone());
		member.setWorkPhone(getWorkPhone());
		member.setCellPhone(getCellPhone());
		member.setMemberType(MemberType.MailOnly);
		logger.info("Signup " + member);

		em.persist(member);	// make member managed
		int numYears = 1;
		MemberTransaction tx = new MemberTransaction(member, numYears, TransactionReason.N, null);
		em.persist(tx);
		member.getMemberTransactions().add(tx);
		em.flush();

		// Survive the redirect so the confirmation shows on the next page
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.getExternalContext().getFlash().setKeepMessages(true);
		fc.addMessage(null, new FacesMessage(
				member.getName() + " signed up, pending confirmation."));

		return VIEW_PAGE + WebConstants.FORCE_REDIRECT;
	}

	private String fail(String message) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
		return null;	// Stay on page (try again?)
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
}
