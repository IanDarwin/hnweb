package action;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Random;

@Path("/")
public class UserPasswordChangeRest {

    static String MESSAGE_TEMPLATE = """
                You (or somebody using your account) requested a password change for user %s on site %s.
                If this was you and you want to proceed, please confirm your email by
                <a href="%s/ChangePassword2.html">following this link</a>.
                If it wasn't you, please just ignore this email.""";

    final static String host = "https://hn.darwinsys.com";

    @Path("changePassword1")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response changePassword1(@FormParam("email") String email) {
        String token = randomString(20);
        System.out.println("In chP1, token = " + token);
        sendPart1Email(email, token);
        var redirectURI = URI.create(host);
        return Response.seeOther(redirectURI).build();
    }

    @Path("changePassword2")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public void changePassword2(@FormParam("token") String token) {
        System.out.println("In chP2, got token=" + token);
    }

    void sendPart1Email(String email, String token) {
        String message = MESSAGE_TEMPLATE.formatted(email, host, host);
        System.out.printf("Would send %s to %s\n", message, email);
        // XXX
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


