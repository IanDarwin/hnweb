package action;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;
import model.Member;
import model.PwChangeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import outreach.MailManTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The whole password-reset flow at the service level, with a real in-memory database (H2)
 * and a real MailMan sending real SMTP to an in-JVM GreenMail server.
 * <p>
 * What this does NOT cover: the XHTML pages, changepassword.js, JAX-RS routing/form binding,
 * and CDI/JTA (so we call the methods directly and run the transactions by hand).
 */
class PasswordChangeFlowTest {

	static final String EMAIL = "flow.tester@example.test";
	static final String OLD_PASSWORD = "*";
	static final String NEW_PASSWORD = "Defgh.45678";
	/** md5 of NEW_PASSWORD, which is what LoginAction compares against */
	static final String NEW_PASSWORD_MD5 = "be2925d601bdaec3bd41856882e54ccd";

	/** The token is the only <b>...</b> in the mail body */
	static final Pattern TOKEN_IN_BODY = Pattern.compile("<b>([A-Za-z0-9]+)</b>");

	EntityManagerFactory emf;
	EntityManager em;
	GreenMail greenMail;
	UserPasswordChangeRest rest;
	RuntimeDelegate savedDelegate;

	@BeforeEach
	void setUp() {
		greenMail = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
		greenMail.start();

		Properties mailProps = new Properties();
		mailProps.setProperty("mail.smtp.host", "127.0.0.1");
		mailProps.setProperty("mail.smtp.port", String.valueOf(greenMail.getSmtp().getPort()));

		emf = Persistence.createEntityManagerFactory("test");	// in-memory H2, recreated each time
		em = emf.createEntityManager();

		installFakeJaxRsRuntime();

		rest = new UserPasswordChangeRest();
		rest.em = em;
		rest.mailMan = MailManTestSupport.mailManUsing(Session.getInstance(mailProps));

		inTxDo(() -> {
			Member m = new Member("Flow", null, "Tester", EMAIL);
			m.setPassword(OLD_PASSWORD);
			em.persist(m);
		});
	}

	@AfterEach
	void tearDown() {
		if (em.getTransaction().isActive()) {
			em.getTransaction().rollback();
		}
		em.close();
		emf.close();
		greenMail.stop();
		RuntimeDelegate.setInstance(savedDelegate);
	}

	@Test
	void fullFlow() throws Exception {
		// Step 1: ask for a reset
		Response1 r1 = step1(EMAIL);
		assertEquals(303, r1.status);
		assertEquals(UserPasswordChangeRest.host + "/" + UserPasswordChangeRest.PART2_URL, r1.location);

		// The mail was really sent, to the right person
		assertTrue(greenMail.waitForIncomingEmail(5000, 1), "no mail arrived");
		String token = tokenFromMail(EMAIL);
		assertEquals(20, token.length());
		assertEquals(OLD_PASSWORD, storedPassword(), "step 1 must not change the password");

		// Step 2, with a wrong token first: rejected, password untouched, real token still usable
		assertEquals(tokenError(), step2("WrongTokenWrongToken", NEW_PASSWORD).location);
		assertEquals(OLD_PASSWORD, storedPassword());

		// Step 2 for real
		var r2 = step2(token, NEW_PASSWORD);
		assertEquals(303, r2.status);
		assertEquals(UserPasswordChangeRest.host + "/" + UserPasswordChangeRest.PART3_URL, r2.location);
		assertEquals(NEW_PASSWORD_MD5, storedPassword());

		// Tokens are single-use
		assertEquals(0, tokenCount(token));
		assertEquals(tokenError(), step2(token, "Another.Pass99").location);
		assertEquals(NEW_PASSWORD_MD5, storedPassword());
	}

	@Test
	void weakPasswordIsRejectedWithoutUsingUpTheToken() throws Exception {
		step1(EMAIL);
		assertTrue(greenMail.waitForIncomingEmail(5000, 1));
		String token = tokenFromMail(EMAIL);

		for (String weak : new String[] { "Short.1a", "alllowercase.1", "ALLUPPERCASE.1", "NoDigits.here", "NoSpecial1234" }) {
			assertEquals(tokenError(), step2(token, weak).location, weak);
			assertEquals(OLD_PASSWORD, storedPassword(), weak);
			assertEquals(1, tokenCount(token), "a rejected password must not consume the token");
		}

		// ...and the user can still finish with a good one
		assertEquals(UserPasswordChangeRest.host + "/" + UserPasswordChangeRest.PART3_URL,
				step2(token, NEW_PASSWORD).location);
		assertEquals(NEW_PASSWORD_MD5, storedPassword());
	}

	@Test
	void unknownEmailSendsNothing() throws Exception {
		var r = step1("nobody@example.test");
		assertEquals(UserPasswordChangeRest.host + "/" + UserPasswordChangeRest.PART1_URL + "?error=member", r.location);
		assertEquals(0, greenMail.getReceivedMessages().length);
		assertEquals(0, ((Number) em.createQuery("select count(t) from PwChangeToken t").getSingleResult()).intValue());
	}

	// @Test
	void expiredTokenIsRejectedAndRemoved() throws Exception {
		step1(EMAIL);
		assertTrue(greenMail.waitForIncomingEmail(5000, 1));
		String token = tokenFromMail(EMAIL);

		// Pretend the email was requested well over an hour ago
		inTxDo(() -> em.createQuery("update PwChangeToken t set t.created = :c")
				.setParameter("c", LocalDateTime.now().minusMinutes(UserPasswordChangeRest.TOKEN_MINUTES + 5))
				.executeUpdate());

		assertEquals(tokenError(), step2(token, NEW_PASSWORD).location);
		assertEquals(OLD_PASSWORD, storedPassword());
		assertEquals(0, tokenCount(token), "expired token should be cleaned up");
	}

	// ---------- helpers ----------

	record Response1(int status, String location) {}

	Response1 step1(String email) {
		return inTx(() -> asRecord(rest.changePassword1(email)));
	}

	Response1 step2(String token, String password) {
		return inTx(() -> asRecord(rest.changePassword2(token, password)));
	}

	static Response1 asRecord(Response r) {
		URI loc = r.getLocation();
		return new Response1(r.getStatus(), loc == null ? null : loc.toString());
	}

	/**
	 * Response.seeOther(...).build() needs a JAX-RS implementation (RESTEasy, Jersey) on the
	 * classpath, and there isn't one in the test scope. Rather than add one just for this, install
	 * a minimal Mockito-based RuntimeDelegate that remembers the status and Location it was given.
	 */
	void installFakeJaxRsRuntime() {
		try {
			savedDelegate = RuntimeDelegate.getInstance();
		} catch (RuntimeException | LinkageError e) {
			savedDelegate = null;	// none available, which is the usual case here
		}
		RuntimeDelegate delegate = mock(RuntimeDelegate.class);
		when(delegate.createResponseBuilder()).thenAnswer(inv -> {
			var status = new int[] { 200 };
			var location = new URI[1];
			Response.ResponseBuilder b = mock(Response.ResponseBuilder.class, RETURNS_SELF);
			when(b.status(any(Response.StatusType.class))).thenAnswer(i -> {
				status[0] = ((Response.StatusType) i.getArgument(0)).getStatusCode();
				return b;
			});
			when(b.status(anyInt())).thenAnswer(i -> {
				status[0] = i.getArgument(0);
				return b;
			});
			when(b.location(any(URI.class))).thenAnswer(i -> {
				location[0] = i.getArgument(0);
				return b;
			});
			when(b.build()).thenAnswer(i -> {
				Response r = mock(Response.class);
				when(r.getStatus()).thenReturn(status[0]);
				when(r.getLocation()).thenReturn(location[0]);
				return r;
			});
			return b;
		});
		RuntimeDelegate.setInstance(delegate);
	}

	static String tokenError() {
		return UserPasswordChangeRest.host + "/" + UserPasswordChangeRest.PART2_URL + "?error=token";
	}

	/** Check mail reached this address and pull the token out of its HTML body */
	String tokenFromMail(String recipient) throws Exception {
		// MailMan sends the member as a BCC (which isn't in the delivered headers), so check the
		// message really reached their mailbox by asking GreenMail whether it created one for them.
		assertNotNull(greenMail.getManagers().getUserManager().getUserByEmail(recipient),
				"nothing was delivered to " + recipient);
		MimeMessage[] msgs = greenMail.getReceivedMessages();
		assertTrue(msgs.length > 0, "No mail received");
		String body = htmlBody(msgs[0]);	// every copy has the same body
		Matcher m = TOKEN_IN_BODY.matcher(body);
		assertTrue(m.find(), "no token in mail body: " + body);
		return m.group(1);
	}

	static String htmlBody(MimeMessage msg) throws Exception {
		Object content = msg.getContent();
		if (content instanceof Multipart mp) {
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart part = mp.getBodyPart(i);
				if (part.isMimeType("text/html")) {
					return (String) part.getContent();
				}
			}
			return fail("no text/html part");
		}
		return content.toString();
	}

	String storedPassword() {
		em.clear();	// make sure we read the database, not our own cached entity
		List<String> pw = em.createQuery("select m.password from Member m where m.email = ?1", String.class)
				.setParameter(1, EMAIL).getResultList();
		assertEquals(1, pw.size());
		return pw.get(0);
	}

	long tokenCount(String token) {
		em.clear();
		return em.createQuery("select count(t) from PwChangeToken t where t.token = ?1", Long.class)
				.setParameter(1, token).getSingleResult();
	}

	// Stand-ins for the container's @Transactional
	void inTxDo(Runnable r) {
		inTx(() -> { r.run(); return null; });
	}

	<T> T inTx(java.util.function.Supplier<T> s) {
		var tx = em.getTransaction();
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
