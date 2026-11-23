package data;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import action.SignupService;
import action.SignupService.BeginStatus;
import action.SignupService.ConfirmStatus;
import jakarta.persistence.EntityTransaction;
import model.ChangeToken;
import model.Member;
import model.MemberType;
import model.UnconfirmedMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Sign up, then confirm: the database side of the signup flow. Same style as MemberArchiveRestoreTest. */
class SignupServiceTest extends AbstractPersistenceTest {

	SignupService service;
	MemberDAO memberDAO;

	@BeforeEach
	void setUpService() {
		memberDAO = new MemberDAO(getEntityManager());
		service = new SignupService(getEntityManager(), memberDAO);
	}

	static UnconfirmedMember signup(String email) {
		var u = new UnconfirmedMember();
		u.setFirstName("Nora");
		u.setLastName("Newcomer");
		u.setEmail(email);
		u.setCity("Barrie");
		u.setMemberType(MemberType.Family);
		u.setNotes("Signed up online.");
		return u;
	}

	@Test
	void signupIsUnconfirmedAndInvisibleAsAMember() {
		var begun = inTx(() -> service.begin(signup("nora@example.test")));
		getEntityManager().clear();

		assertEquals(BeginStatus.TOKEN_CREATED, begun.status());
		assertEquals(32, begun.token().length());
		assertEquals("Unconfirmed", typeOf("nora@example.test"));
		assertEquals(0, count("select count(m) from Member m where m.email = 'nora@example.test'"));
		assertEquals(1, count("select count(m) from UnconfirmedMember m where m.email = 'nora@example.test'"));
	}

	@Test
	void confirmingTurnsItIntoARealMemberAndUsesUpTheToken() {
		var begun = inTx(() -> service.begin(signup("nora@example.test")));
		getEntityManager().clear();

		var c = inTx(() -> service.confirm(begun.token()));
		getEntityManager().clear();

		assertEquals(ConfirmStatus.CONFIRMED, c.status());
		assertEquals("Nora Newcomer", c.name());
		assertEquals("Member", typeOf("nora@example.test"));
		Member m = getEntityManager().createQuery("select m from Member m where m.email = 'nora@example.test'", Member.class)
			.getSingleResult();
		assertEquals("Barrie", m.getCity());
		assertEquals(MemberType.Family, m.getMemberType());
		assertEquals(LocalDate.now().getYear(), m.getYearJoined());
		assertEquals(0, m.getExpiryYear(), "not paid up until the treasurer records a payment");

		// the link is single-use
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm(begun.token())).status());
	}

	@Test
	void unknownOrBlankTokensAreInvalid() {
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm("nope")).status());
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm("")).status());
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm(null)).status());
	}

	@Test
	void anExpiredLinkDoesNotConfirm() {
		var begun = inTx(() -> service.begin(signup("nora@example.test")));
		inTx(() -> getEntityManager().createQuery("update ChangeToken t set t.created = :c")
			.setParameter("c", LocalDateTime.now().minusHours(SignupService.TOKEN_HOURS + 1))
			.executeUpdate());
		getEntityManager().clear();

		assertEquals(ConfirmStatus.EXPIRED, inTx(() -> service.confirm(begun.token())).status());
		getEntityManager().clear();
		assertEquals("Unconfirmed", typeOf("nora@example.test"));
		// and the dead link can't be retried
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm(begun.token())).status());
	}

	@Test
	void signingUpAgainReplacesTheEarlierAttemptAndItsLink() {
		var first = inTx(() -> service.begin(signup("nora@example.test")));
		getEntityManager().clear();
		var second = inTx(() -> service.begin(signup("NORA@example.test")));	// case differs
		getEntityManager().clear();

		assertEquals(BeginStatus.TOKEN_CREATED, second.status());
		assertEquals(1, count("select count(m) from UnconfirmedMember m"));
		assertEquals(1, count("select count(t) from ChangeToken t"));
		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm(first.token())).status());
		assertEquals(ConfirmStatus.CONFIRMED, inTx(() -> service.confirm(second.token())).status());
	}

	@Test
	void anEmailThatBelongsToAMemberIsNotSignedUpAgain() {
		persistRealMember("rita@example.test");
		var begun = inTx(() -> service.begin(signup("RITA@example.test")));	// case differs, still matches
		getEntityManager().clear();

		assertEquals(BeginStatus.ALREADY_MEMBER, begun.status());
		assertNull(begun.token());
		assertEquals(0, count("select count(m) from UnconfirmedMember m"));
		assertEquals(0, count("select count(t) from ChangeToken t"));
		assertEquals("Member", typeOf("rita@example.test"));
	}

	@Test
	void removingOldSignupsNeverTouchesARealMemberWithTheSameEmail() {
		persistRealMember("rita@example.test");
		int deleted = inTx(() -> getEntityManager().createQuery(
				"delete from UnconfirmedMember u where lower(u.email) = 'rita@example.test'").executeUpdate());
		assertEquals(0, deleted);
		assertEquals("Member", typeOf("rita@example.test"));
	}

	@Test
	void aPasswordResetTokenCannotConfirmASignup() {
		inTx(() -> service.begin(signup("nora@example.test")));
		inTx(() -> {
			getEntityManager().persist(new ChangeToken("nora@example.test", "pwtoken-pwtoken-pw", ChangeToken.PURPOSE_PASSWORD));
			return null;
		});
		getEntityManager().clear();

		assertEquals(ConfirmStatus.INVALID, inTx(() -> service.confirm("pwtoken-pwtoken-pw")).status());
		assertEquals("Unconfirmed", typeOf("nora@example.test"));
	}

	// ----- helpers -----

	private void persistRealMember(String email) {
		var m = new Member("Rita", null, "Real", email);
		m.setMemberType(MemberType.Individual);
		inTx(() -> { getEntityManager().persist(m); return null; });
		getEntityManager().clear();
	}

	private String typeOf(String email) {
		return (String) getEntityManager().createNativeQuery("select type from member where email = ?1")
			.setParameter(1, email)
			.getSingleResult();
	}

	private long count(String jpql) {
		return getEntityManager().createQuery(jpql, Long.class).getSingleResult();
	}

	private <T> T inTx(Supplier<T> s) {
		EntityTransaction tx = getEntityManager().getTransaction();
		tx.begin();
		try {
			T result = s.get();
			tx.commit();
			return result;
		} catch (RuntimeException e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
	}
}
