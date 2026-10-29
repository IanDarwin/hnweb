package action;

import jakarta.servlet.annotation.WebServlet;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.Random;

@Path("")
public class UserPasswordChangeRest {

    final static String host = "https://darwinsys.com";

    @Path("changePassword1")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public void changePassword1(@FormParam("email") String email) {
        String token = randomString(20);
        System.out.println("In chpw1, token = " + token);
        sendPart1Email(email, token);
    }

    @Path("changePassword2")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public void changePassword2(@FormParam("token") String token) {
        System.out.println("In chpw2, token=" + token);
    }

    void sendPart1Email(String email, String token) {
        String message = """
                You (or somebody using your account) requested a password change for user %s on site %s.
                If this was you aand you wanto to proceed, please confirm your email by
                <a href="%s/ChangePassword2.html">following this link.
                If it wasn't you, please just ignore this email.
                """.formatted(host, email, host);
        System.out.printf("Would send %s to %s\n", message, email);
        // XXX
    }
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


