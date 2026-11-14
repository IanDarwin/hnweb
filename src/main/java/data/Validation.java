package data;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.darwinsys.lang.StringHolder;

import model.Country;

/**
 * Raw validators, not tied to JSF or anything.
 * @author Ian Darwin
 */
public class Validation implements Serializable {

	private static final long serialVersionUID = -12845872132631791L;

	/// Pattern for checking email
	private static Pattern emailPattern = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	/** Validate that an email address is well-formed (it may or may not be deliverable).
	 * @param email The email address to be validated
	 * @param messageHolder The response holder
	 * @return True if the message is not malformed.
	 */
	public static boolean validateEmail(String email, StringHolder messageHolder) {
		boolean valid = true;
		if (email == null || email.length() == 0)
			return false; // Or throw NPE?
		if (emailPattern.matcher(email).matches()) {
			return true;
		}
		messageHolder.value = email + "looks like invalid email syntax.";
		return false;
	}

	/** 
	 * Validate a Post Code
	 * @param country The country
	 * @param postCode The post code
	 * @param invalidMessage A holder for a message if invalid
	 * @return The syntactic validity of the given code
	 */
	public static boolean validatePostCode(Country country, String postCode, StringHolder invalidMessage) {
		boolean valid = true;
		switch(country) {
		case CA:
			valid = postCode.matches("\\w\\d\\w ?\\d\\w\\d");
			invalidMessage.value = "PostCode must match ANA NAN pattern";
			break;
		case US:
			valid = postCode.matches("\\d{5}(-\\d{4})?");
			invalidMessage.value = "Zip Code must be 5 digits, optional -4 digits";
			break;
		case ZZ:
			break;
		default:
			throw new IllegalArgumentException("Unknown country");
		}
		return valid;
	}
}
