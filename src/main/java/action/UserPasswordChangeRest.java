package action;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
import java.util.Random;

@Path("/")
public class UserPasswordChangeRest {

    static String MESSAGE_TEMPLATE = """
                You (or somebody using your account) requested a password change for user %s on site %s.
                If this was you and you want to proceed, please confirm your email
				by pasting this token into the next page, along with your new password.
                <br/><b>%s</b><br/>
                If you didn't request this, please just ignore this email.""";

    final static String host = "https://hn.darwinsys.com";
	final static String PART2_URL = "ChangePassword2.web";

    @Inject
    MailMan mailMan;
    @PersistenceContext
    EntityManager em;

    @Path("changePassword1")
    @POST
	@Transactional
    @Consumes("application/x-www-form-urlencoded")
    public Response changePassword1(@FormParam("email") String email) {
        String token = randomString(20);
        System.out.println("In chP1, token = " + token);
        em.persist(new PwChangeToken(email, token));
        String message = MESSAGE_TEMPLATE.formatted(email, host, host, token);
        System.out.printf("Sending %s to %s\n", message, email);
        try {
            mailMan.sendMessageTo(email, "HN Passord Reset", message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to email " + email, e);
        }
        // XXX FacesContext.getCurrentInstance().addMessage( null, new FacesMessage("Watch your email for the next step."));
        var redirectURI = URI.create(host + "/" + PART2_URL);
        return Response.seeOther(redirectURI).build();
    }

    @Path("changePassword2")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public void changePassword2(@FormParam("token") String token) {
        System.out.println("In chP2, got token=" + token);
    }

    // Replace w/ $dsapi/genpws
    public String randomString(int targetStringLength) {
        if (targetStringLength < 10) {
            throw new IllegalStateException(targetStringLength + " is too short");
        }
        int leftLimit = 48 + 2; // numeral '0', but skip 0 and 1
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
          .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) && i != 154) // Omit l/c L
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    }
}


