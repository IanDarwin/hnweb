package action;

import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import model.PwChangeToken;
import outreach.MailMan;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

import com.darwinsys.security.DigestUtils;
import model.Member;

@Path("/")
public class UserPasswordChangeRest {

    /** The email we send the user with their token.
     * NOTA BENA: Do NOT add any 'b' tags or unless you change the tests!
     */
    static String MESSAGE_TEMPLATE = """
                You (or somebody using your account) requested a password change for user %s on site %s.
                If this was you and you want to proceed, please confirm your email
				by pasting this token into the next page, along with your new password.
                <br/><b>%s</b><br/>
                If you didn't request this, please just ignore this email.""";

    /** How long an emailed token stays valid */
    final static int TOKEN_MINUTES = 60;

    final static String host = "https://hn.darwinsys.com";
	final static String PART1_URL = "ChangePassword1.web";
	final static String PART2_URL = "ChangePassword2.web";
	final static String PART3_URL = "ChangePassword3.web";

    @Inject
    MailMan mailMan;
    @PersistenceContext
    EntityManager em;

    @Path("changePassword1")
    @POST
	@Transactional
    @Consumes("application/x-www-form-urlencoded")
    public Response changePassword1(@FormParam("email") String email) {
		var members = em.createQuery("SELECT m FROM Member m WHERE m.email = ?1", Member.class)
			.setParameter(1, email)
			.setMaxResults(2)
			.getResultList();
        if (members.size() != 1) {
            System.err.println("changePassword1: no unique member for token's email");
            return badMember();
        }
        String token = randomString(20);
        System.out.println("In chP1, token = " + token);
        em.persist(new PwChangeToken(email, token));
        String message = MESSAGE_TEMPLATE.formatted(email, host, token);
        try {
            mailMan.sendMessageTo(email, "HN Passord Reset", message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to email " + email, e);
        }
        var redirectURI = URI.create(host + "/" + PART2_URL);
        return Response.seeOther(redirectURI).build();
    }

    /** Back to step 1 with a generic message */
    private Response badMember() {
        return Response.seeOther(URI.create(host + "/" + PART1_URL + "?error=member")).build();
    }

    @Path("changePassword2")
    @POST
    @Transactional
    @Consumes("application/x-www-form-urlencoded")
    public Response changePassword2(@FormParam("token") String token, @FormParam("newPassword") String newPassword) {
        // Every failure below goes to the same vague "try again" message: users
        // can't act on JPA details, and we shouldn't tell an attacker which part failed.
        if (token == null || token.isBlank() || newPassword == null || newPassword.isEmpty()) {
            return badToken();
        }

        if (!meetsPasswordRules(newPassword)) {
            return badToken();
        }

        // XXX Simplify to getSingleResultOrNull() when Wildfly version provides JPA 3.2 
        var tokens = em.createQuery("SELECT pw FROM PwChangeToken pw WHERE pw.token = ?1", PwChangeToken.class)
                .setParameter(1, token.trim())
                .setMaxResults(2)
                .getResultList();
        if (tokens.size() != 1) {
            return badToken();
        }
        PwChangeToken pwToken = tokens.get(0);

        // Tokens are single-use: remove it now, whatever happens next
        em.remove(pwToken);

        var created = pwToken.getCreated();
        if (created == null || created.plusMinutes(TOKEN_MINUTES).isBefore(LocalDateTime.now())) {
            return badToken();
        }

        var members = em.createQuery("SELECT m FROM Member m WHERE m.email = ?1", Member.class)
                .setParameter(1, pwToken.getEmail())
                .setMaxResults(2)
                .getResultList();
        if (members.size() != 1) {
            System.err.println("changePassword2: no unique member for token's email");
            return badToken();
        }

        members.get(0).setPassword(DigestUtils.md5(newPassword)); // What we came for!

        return Response.seeOther(URI.create(host + "/" + PART3_URL)).build();
    }

    // Same rules as js/changepassword.js, which must be kept in step with these.
    private static final Pattern UPPER = Pattern.compile("\\p{Lu}");
    private static final Pattern LOWER = Pattern.compile("\\p{Ll}");
    private static final Pattern DIGIT = Pattern.compile("\\p{Nd}");
    private static final Pattern SPECIAL = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    /**
     * Server-side enforcement of the password rules (the JavaScript can be bypassed).
     * Note find(), not matches(): matches() requires the WHOLE string to match the pattern.
     */
    static boolean meetsPasswordRules(String pw) {
        return pw != null
                && pw.length() >= 10
                && UPPER.matcher(pw).find()
                && LOWER.matcher(pw).find()
                && DIGIT.matcher(pw).find()
                && SPECIAL.matcher(pw).find();
    }

    /** Back to step 2 with a generic message */
    private Response badToken() {
        return Response.seeOther(URI.create(host + "/" + PART2_URL + "?error=token")).build();
    }

    // Replace w/ $dsapi/genpwds
    public String randomString(int targetStringLength) {
        if (targetStringLength < 10) {
            throw new IllegalStateException(targetStringLength + " is too short");
        }
        int leftLimit = 48 + 2; // numeral '0', but skip 0 and 1
        int rightLimit = 122; // letter 'z'
        Random random = new SecureRandom(); // tokens must not be guessable

        return random.ints(leftLimit, rightLimit + 1)
          .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) && i != 108) // Omit l/c L
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    }
}


