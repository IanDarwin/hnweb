package action;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import data.MemberDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import model.ChangeToken;
import model.MemberBase;
import model.MemberType;
import model.UnconfirmedMember;

/**
 * The database side of signing up as a new member, kept free of JSF so it can be tested.
 * <p>
 * Step 1, {@link #begin}: the signup is saved as an {@link UnconfirmedMember} (same table as
 * Member, different "type", so it appears in no member list or mailing) with a
 * single-use token for the link we email.
 * Step 2, {@link #confirm}: the token is checked and consumed, and the row is flipped to a real Member.
 * <p>
 * A confirmed signup is a Member with expiryYear 0 (not paid up) until the treasurer records
 * the payment (admin Renew page).
 */
@ApplicationScoped
public class SignupService {

	/** How long the emailed link works */
	public static final int TOKEN_HOURS = 72;

	private static final SecureRandom RANDOM = new SecureRandom();

	@PersistenceContext
	EntityManager em;
	@Inject
	MemberDAO memberDAO;

	/** For CDI */
	public SignupService() {
	}

	/** For tests, outside a container */
	public SignupService(EntityManager em, MemberDAO memberDAO) {
		this.em = em;
		this.memberDAO = memberDAO;
	}

	public enum BeginStatus {
		/** Saved; email the link containing the token */
		TOKEN_CREATED,
		/** That email already belongs to a member (current or archived); nothing saved */
		ALREADY_MEMBER
	}

	public record Begun(BeginStatus status, String token) {
	}

	public enum ConfirmStatus {
		CONFIRMED, INVALID, EXPIRED
	}

	/** What the confirmation did; the details are for the welcome notice, and are null unless CONFIRMED */
	public record Confirmation(ConfirmStatus status, String name, String email, MemberType type) {
	}

	/**
	 * Save a new signup, replacing any earlier unconfirmed attempt with the same email
	 * (so a person who never got the email, or lost it, can simply sign up again).
	 *
	 * @param signup A filled-in, not yet saved UnconfirmedMember
	 */
	@Transactional
	public Begun begin(UnconfirmedMember signup) {
		String email = signup.getEmail().trim();
		signup.setEmail(email);
		// signup.setCreation(LocalDate.now()); // done by the database, should do here too.

		List<MemberBase> existing = em.createQuery(
				"select m from MemberBase m where lower(m.email) = :e", MemberBase.class)
			.setParameter("e", email.toLowerCase())
			.getResultList();
		// Any actual (non-Unconfirmed) members will trigger this.
		for (MemberBase m : existing) {
			if (!(m instanceof UnconfirmedMember)) {
				return new Begun(BeginStatus.ALREADY_MEMBER, null);
			}
		}
		// Earlier unconfirmed attempts, and their links, are replaced.
		// This statement says "UnconfirmedMember", so Hibernate adds type = 'Unconfirmed' to the SQL.
		// em.remove(old) would delete by id alone: if that row had just been confirmed by another
		// request, we would delete the real member.
		existing.forEach(em::detach);
		em.createQuery("delete from UnconfirmedMember u where lower(u.email) = :e")
			.setParameter("e", email.toLowerCase())
			.executeUpdate();
		em.createQuery("delete from ChangeToken t where lower(t.email) = :e and t.purpose = :p")
			.setParameter("e", email.toLowerCase())
			.setParameter("p", ChangeToken.PURPOSE_SIGNUP)
			.executeUpdate();
		em.flush();		// the old row must be gone before the new one claims the (unique) email

		em.persist(signup);

		String token = newToken();
		em.persist(new ChangeToken(email, token, ChangeToken.PURPOSE_SIGNUP));
		return new Begun(BeginStatus.TOKEN_CREATED, token);
	}

	/**
	 * Confirm a signup from the emailed link. The token is single-use: it is consumed
	 * here whether or not it turns out to be expired.
	 */
	@Transactional
	public Confirmation confirm(String token) {
		var invalid = new Confirmation(ConfirmStatus.INVALID, null, null, null);
		if (token == null || token.isBlank()) {
			return invalid;
		}
		List<ChangeToken> tokens = em.createQuery(
				"select t from ChangeToken t where t.token = :t and t.purpose = :p", ChangeToken.class)
			.setParameter("t", token.trim())
			.setParameter("p", ChangeToken.PURPOSE_SIGNUP)
			.setMaxResults(2)
			.getResultList();
		if (tokens.size() != 1) {
			return invalid;
		}
		ChangeToken t = tokens.get(0);
		em.remove(t);

		if (t.getCreated() == null || t.getCreated().plusHours(TOKEN_HOURS).isBefore(LocalDateTime.now())) {
			return new Confirmation(ConfirmStatus.EXPIRED, null, null, null);
		}
		List<UnconfirmedMember> found = em.createQuery(
				"select u from UnconfirmedMember u where lower(u.email) = :e", UnconfirmedMember.class)
			.setParameter("e", t.getEmail().toLowerCase())
			.setMaxResults(2)
			.getResultList();
		if (found.size() != 1) {
			return invalid;
		}
		UnconfirmedMember u = found.get(0);
		String name = u.getFirstName() + " " + u.getLastName();
		String email = u.getEmail();
		MemberType type = u.getMemberType();

		u.setYearJoined(LocalDate.now().getYear());
		em.flush();		// send our changes before MemberDAO goes around JPA
		memberDAO.flipMemberType(u, "Member");
		return new Confirmation(ConfirmStatus.CONFIRMED, name, email, type);
	}

	/** 24 random bytes as 32 URL-safe characters: fine in a link, not guessable */
	static String newToken() {
		byte[] b = new byte[24];
		RANDOM.nextBytes(b);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
	}
}
