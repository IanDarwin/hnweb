package data;

import java.util.Arrays;
import java.util.List;

import com.darwinsys.lang.StringHolder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertSame;

public class MemberValidatorEmailTest {
	
	String email;
	boolean expected;
	StringHolder messageHolder = new StringHolder();

	public void initMemberValidatorEmailTest(boolean expected, String email) {
		this.email = email;
		this.expected = expected;
	}
	
	public static List<Object[]> getParams() {
		return Arrays.asList(new Object[][]{
				// Null or Empty are valid for us, might not be for you.
				{true, null },
				{true, "" },
				// From short to long, roughly
				{true, "a@b.co"},
				{false, "a at b.co" },
				{true, "a.b.c@d.e.f.com"},
				{true, "ketchup@mcdonalds.infomaterial"},
				{true,  "hey-gal@sympatico-online.ca"},
		});
	}

	@MethodSource("getParams")
	@ParameterizedTest
	public void test(boolean expected, String email) {
		initMemberValidatorEmailTest(expected, email);
		System.out.println("Testing: " + email);
		assertSame(expected, MemberValidator.validateEmail(email, messageHolder));
	}

}
