package data;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.omg.CORBA.StringHolder;

@RunWith(Parameterized.class)
public class PersonHomeValidateEmailTest {
	
	String email;
	boolean expected;
	StringHolder messageHolder = new StringHolder();
	
	public PersonHomeValidateEmailTest(boolean expected, String email) {
		this.email = email;
		this.expected = expected;
	}
	
	private final static Object[][] data = {
		// Null or Empty are valid for us, might not be for you.
		{true, null },
		{true, "" },
		// From short to long, roughly
		{true, "a@b.co"},
		{false, "a at b.co" },
		{true, "a.b.c@d.e.f.com"},
		{true, "ketchup@mcdonalds.infomaterial"},
		{false, "a-b@c.com"},
	};
	
	@Parameters
	public static List<Object[]> getParams() {
		return Arrays.asList(data);
	}	
	
	@Test
	public void test() {
		assertSame(expected, PersonHome.validateEmail(email, messageHolder));
	}

}
