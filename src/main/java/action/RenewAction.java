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
import model.Member;
import model.MemberTransaction;
import model.PaymentType;
import model.TransactionReason;
import webtier.WebConstants;

/**
 * Backs Renew.xhtml.
 * <p>
 * This is a request-scoped CDI bean, NOT a @Stateless EJB: it holds the
 * form's fields (member, years, payment type) between the setter calls and
 * the call to {@link #renew()}, and a stateless bean pool does not promise
 * that all those calls land on the same instance.
 */
@Named
@RequestScoped
public class RenewAction implements Serializable {

	private static final long serialVersionUID = 832908490238178971L;

	private static final Logger logger = Logger.getLogger(RenewAction.class.getName());

	private static final String VIEW_PAGE = "MemberView";

	private Member member;
	private int numYears = 1;
	private PaymentType paymentType = PaymentType.CASH;

	@PersistenceContext
	transient EntityManager em;

	/**
	 * Record a renewal for the chosen member.
	 * @return The outcome: the member's page on success; null (stay here) on error.
	 */
	@Transactional
	public String renew() {
		logger.info(() -> "renew() for " + member + " for " + numYears + " year(s)");
		if (member == null) {
			return fail("Choose a member from the list first.");
		}
		if (numYears <= 0) {
			return fail("Choose how many years to renew for.");
		}

		member = em.merge(member);	// make member managed
		MemberTransaction tx = new MemberTransaction(member, numYears, TransactionReason.R, paymentType);
		em.persist(tx);
		member.getMemberTransactions().add(tx);
		member.acceptRenewal(tx);
		em.flush();
		// XXX Needs more post-split refactoring: also renew a Family master's secondaries

		// Survive the redirect so the confirmation shows on the next page
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.getExternalContext().getFlash().setKeepMessages(true);
		fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				member.getName() + " renewed through " + member.getExpiryYear() + ".", null));

		return VIEW_PAGE + WebConstants.FORCE_REDIRECT + "&memberId=" + member.getId();
	}

	private String fail(String message) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
		return null;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public int getNumYears() {
		return numYears;
	}

	public void setNumYears(int numYears) {
		this.numYears = numYears;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
}
