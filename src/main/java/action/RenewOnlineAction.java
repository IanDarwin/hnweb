package action;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import model.MemberType;
import org.primefaces.PrimeFaces;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import data.MemberDAO;
import model.Member;
import outreach.MailMan;
import payment.HelcimClient;
import payment.PaymentException;
import webtier.LoginConstants;
import webtier.WebConstants;

/**
 * Backs Renew.xhtml: a logged-in member renews, and pays by card through HelcimPay.js.
 * <p>
 * Steps: startPayment() asks Helcim for a checkout session (amount decided HERE, not by the page)
 * and opens Helcim's payment window; when the window reports success, the page's script puts the
 * report in the hidden field and submits; completePayment() verifies it (hash, status, amount)
 * and only then records the renewal.
 */
@Named
@ViewScoped
public class RenewOnlineAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RenewOnlineAction.class.getName());

	private static final Pattern SAFE_TOKEN = Pattern.compile("[A-Za-z0-9_-]{8,128}");

	@Inject
	HelcimClient helcim;
	@Inject
	RenewalService service;
	@Inject
	MemberDAO memberDAO;
	@Inject
	MailMan mailMan;

	private RenewalService.Info info;
	private String problem;			// why this member can't renew online, or null
	private int numYears = 1;

	/** Server-side memory of the payment in progress. The secret token never goes to the browser. */
	private HelcimClient.Checkout checkout;
	private BigDecimal pendingAmount;
	private int pendingYears;

	/** The Helcim window's success report, put here by the page's script */
	private String result;

	@PostConstruct
	void init() {
		Member member = null;
		var ctx = FacesContext.getCurrentInstance().getExternalContext();
		Object o = ctx.getSessionMap().get(LoginConstants.LOGIN_MEMBER);
		if (o instanceof Member m) {
			member = m;
			if (member.getMemberType() == MemberType.Secondary) {
				// read-only, should work without a transaction.
				member = member.getMasterMember();
			}
		}
		Long id = member != null ? member.getId() : null;
		info = service.lookup(id);
		if (!helcim.isConfigured()) {
			problem = "Online payment isn't set up yet. Please renew by mail or in person.";
		} else if (info == null) {
			problem = "We couldn't find your membership record. Please contact us.";
		} else if (RenewalService.annualFee(info.type()) == null) {
			problem = "Your membership type can't be renewed online. Please contact us.";
		}
	}

	// ---------- the two actions ----------

	/** Ask Helcim for a checkout session and open its payment window. */
	public void startPayment() {
		if (!isEligible()) {
			return;
		}
		if (numYears < 1 || numYears > 3) {
			error("Choose 1, 2 or 3 years.");
			return;
		}
		try {
			pendingAmount = getTotal();
			pendingYears = numYears;
			checkout = helcim.initialize(pendingAmount, RenewalService.currency());
			if (!SAFE_TOKEN.matcher(checkout.checkoutToken()).matches()) {
				checkout = null;
				throw new PaymentException("Unexpected checkout token format");
			}
			PrimeFaces.current().executeScript("HNHelcim.start('" + checkout.checkoutToken() + "')");
		} catch (PaymentException e) {
			checkout = null;
			logger.log(Level.WARNING, "Could not start payment for member " + info.memberId(), e);
			error("We couldn't start the payment. Please try again later, or renew by mail or in person.");
		}
	}

	/** Verify what the payment window reported and, only if it checks out, renew. */
	public String completePayment() {
		if (!isEligible() || checkout == null || pendingAmount == null) {
			error("No payment is in progress. Please start again.");
			return null;
		}
		final var session = checkout;
		checkout = null;			// one use only, whatever happens below
		HelcimClient.Payment payment;
		try {
			payment = HelcimClient.validate(result, session.secretToken(), pendingAmount, RenewalService.currency());
		} catch (PaymentException e) {
			logger.log(Level.SEVERE, "Payment verification FAILED for member " + info.memberId()
				+ " (" + e.getMessage() + "). If the member was charged, reconcile in the Helcim dashboard. Result: " + result);
			error("We couldn't verify your payment. If your card was charged, please contact us and "
				+ "we'll sort it out; you will not be charged twice.");
			return null;
		}
		// Money has changed hands: leave a trail before touching the database
		logger.info("Helcim payment verified: " + payment + " for member " + info.memberId()
			+ ", " + pendingYears + " year(s)");
		int newExpiry;
		try {
			newExpiry = service.recordRenewal(info.memberId(), pendingYears, payment);
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "PAID BUT NOT RECORDED: " + payment + " member " + info.memberId()
				+ " years " + pendingYears + ". Record this renewal by hand.", e);
			error("Your payment went through, but we had trouble recording it. Please don't pay again; "
				+ "we've been notified and will update your membership.");
			return null;
		}
		sendReceipt(payment, newExpiry);

		var fc = FacesContext.getCurrentInstance();
		fc.getExternalContext().getFlash().setKeepMessages(true);
		fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
			"Thank you! Your membership now runs through " + newExpiry + ".", null));
		return "/Renew" + WebConstants.FORCE_REDIRECT;
	}

	private void sendReceipt(HelcimClient.Payment p, int newExpiry) {
		try {
			Member m = (Member) FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().get(LoginConstants.LOGIN_MEMBER);
			if (m == null || m.getEmail() == null || m.getEmail().isBlank()) {
				return;
			}
			mailMan.sendMessageTo(m.getEmail(), "Your membership renewal", String.format(
				"Thank you for renewing, %s!<br/>Paid: %s %s (%s, approval %s, reference %s).<br/>"
				+ "Your membership now runs through %d.",
				info.name(), p.amount(), p.currency(), p.cardNumber(), p.approvalCode(), p.transactionId(), newExpiry));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not email receipt for " + p.transactionId(), e);	// the renewal itself is done
		}
	}

	private void error(String text) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null));
	}

	// ---------- for the page ----------

	public boolean isEligible() {
		return problem == null;
	}

	public String getProblem() {
		return problem;
	}

	public String getName() {
		return info == null ? null : info.name();
	}

	public boolean isViaMaster() {
		return info != null && info.viaMaster();
	}

	public Integer getExpiryYear() {
		return info == null ? null : info.expiryYear();
	}

	public BigDecimal getAnnualFee() {
		return info == null ? null : RenewalService.annualFee(info.type());
	}

	public BigDecimal getTotal() {
		var fee = getAnnualFee();
		return fee == null ? null : fee.multiply(BigDecimal.valueOf(numYears)).setScale(2);
	}

	public String getCurrency() {
		return RenewalService.currency();
	}

	public int getNumYears() {
		return numYears;
	}

	public void setNumYears(int numYears) {
		this.numYears = numYears;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
