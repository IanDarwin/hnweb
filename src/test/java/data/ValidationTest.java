package data;

import com.darwinsys.lang.StringHolder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ValidationTest {
	
	String email;
	boolean expected;
	StringHolder messageHolder = new StringHolder();

	@ParameterizedTest(name = "{index} String {0} should give {1}")
	@CsvSource({
			"luser, false",
			"luser@host, false",
			"luser@host.dom, true",
			"donald.duck@dizzney.comix, true",
			"mickey-mouse@dizzney.comix, true",
			// These cases adapted from https://prospeo.io/s/email-syntax-check
			// Commented0-out ones are explained there.
			"user@example.com</code>,true",				// Standard format
			"firstname+tag@example.com</code>,true",	// Plus-addressing is RFC-valid
			"&quot;email&quot;@example.com</code>,true",// Quoted-string local part
			"email@[123.123.123.123]</code>,true",		// IP-literal domain
			"user@example.museum</code>,true",			// Long TLDs are valid
			"!#$%&amp;'*+/=?^_{|}~@example.com</code>,true", // RFC-permitted special chars
			"plainaddress</code>,false",				// Missing @ symbol
			// ".email@example.com</code>,false",			// Leading dot in local part
			// "email..email@example.com</code>,false",	// Consecutive dots
			"email@example@example.com</code>,false",	// Multiple @ symbols
			// "email@-example.com</code>,false",			// Domain starts with hyphen
			// "email@example..com</code>,false",			// Consecutive dots in domain |</p>
	})
	void testIsValidEmail(String s, boolean expect) {
		assertEquals(expect, Validation.validateEmail(s, messageHolder));
	}
}
