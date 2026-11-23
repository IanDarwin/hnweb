package action;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import model.Config;
import model.Member;
import model.MemberTransaction;
import model.MemberType;
import model.PaymentType;
import model.TransactionReason;
import payment.HelcimClient;

/**
 * The database side of self-service renewal: who is renewing, what it costs, and recording
 * a verified payment as a renewal. Kept apart from the JSF bean so the transaction boundary is clear.
 * <p>
 * Fees come from config.properties: renewal.fee.individual, renewal.fee.family, renewal.fee.student
 * (per year; no entry means that type can't renew online) and renewal.currency (default CAD).
 */
@ApplicationScoped
public class RenewalService {

	/** Who would be renewed: a Secondary member's renewal is the Family master's. */
	public record Info(Long memberId, String name, MemberType type, Integer expiryYear, boolean viaMaster)
			implements Serializable {
	}

	@PersistenceContext
	EntityManager em;

	/** @return who gets renewed when this logged-in member renews, or null if not found/eligible */
	public Info lookup(Long loggedInMemberId) {
		if (loggedInMemberId == null) {
			return null;
		}
		Member m = em.find(Member.class, loggedInMemberId);
		if (m == null) {
			return null;
		}
		boolean viaMaster = false;
		if (m.getMemberType() == MemberType.Secondary) {
			if (m.getMasterMember() == null) {
				return null;
			}
			m = m.getMasterMember();
			viaMaster = true;
		}
		return new Info(m.getId(), m.getName(), m.getMemberType(), m.getExpiryYear(), viaMaster);
	}

	/** @return Per-year fee for this type, or null if it can't renew online. */
	public static BigDecimal annualFee(MemberType type) {
		if (type == null || type == MemberType.MailOnly || type == MemberType.Secondary) {
			return null;
		}
		String s = Config.getProperty("renewal.fee." + type.name().toLowerCase());
		if (s == null || s.isBlank()) {
			return null;
		}
		return new BigDecimal(s.trim()).setScale(2, java.math.RoundingMode.HALF_UP);
	}

	public static String currency() {
		String c = Config.getProperty("renewal.currency");
		return (c == null || c.isBlank()) ? "CAD" : c.trim().toUpperCase();
	}

	/**
	 * Record a verified payment as a renewal. Safe against double-recording: a payment
	 * (Helcim transaction id) is recorded at most once, enforced here and by a unique column.
	 *
	 * @return The member's new expiry year
	 * @throws IllegalStateException if this payment was already recorded
	 */
	@Transactional
	public int recordRenewal(Long memberId, int years, HelcimClient.Payment payment) {
		String ref = "helcim:" + payment.transactionId();
		long already = em.createQuery("select count(t) from MemberTransaction t where t.externalRef = ?1", Long.class)
			.setParameter(1, ref)
			.getSingleResult();
		if (already > 0) {
			throw new IllegalStateException("Payment " + ref + " was already recorded");
		}
		Member member = em.find(Member.class, memberId);
		if (member == null) {
			throw new IllegalStateException("No member " + memberId + " for payment " + ref);
		}
		var tx = new MemberTransaction(member, years, TransactionReason.R, PaymentType.CREDIT);
		tx.setExternalRef(ref);
		em.persist(tx);
		member.getMemberTransactions().add(tx);
		member.acceptRenewal(tx);
		em.flush();
		return member.getExpiryYear();
	}
}
