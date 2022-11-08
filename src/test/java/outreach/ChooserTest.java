package outreach;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import model.Member;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ChooserTest {

	final static List<Member> data = new ArrayList<>();
	final static Member m1 = new Member("Foo", "", "Bar", "ipwn@bar.com");
	final static Member m2 = new Member();
	final static Member m3 = new Member();
	final static Member m4 = new Member();

	@BeforeAll
	public static void setup() {
		data.clear();
		m1.setEmail("foo@bar.com");
		data.add(m1);
		m2.setEmail("nobody@all");
		data.add(m2);
		m3.setEmail("donald@duckduckgo");
		data.add(m3);
		m4.setEmail("no@no");
		data.add(m4);
		// Just to make sure MailMan class is loaded.
		if (MailMan.now.getYear() == 1492) {
			throw new IllegalArgumentException("Run away! A slaver cometh over the sae!");
		}
	}

	// Simple convenience routine
	private List<Member> selectListMembers(List<Member> list, Predicate<Member> chooser) {
		return list.stream().filter(chooser).toList();
	}

	/** This has to be just right:
	 * 1) If it is now December, and you renewed in September, you are paid up.
	 * 2) If it is now January, and you expired in September, are not paid up
	 * 3) If it is now January and you expired 15 months ago, you are not paid up
	 * Note the date in the test method before studying this!
	 */
	@ParameterizedTest(name = "String {0} should give {1}")
	@CsvSource({
			"2023, 1",
			"2022, 1",
			"2021, 1",
			"2020, 1",
			"2019, 0",
			"2018, 0",
	})
	public void testPaidMemberChooser(int expiryYear, int expectedMatches) {
		MailMan.now = LocalDate.of(2020, 8, 15);
		Member m = data.get(0);
		m.setExpiryYear(expiryYear);
		assertEquals(expectedMatches, selectListMembers(data, MailMan.paidMemberChooser).size());
	}

	@Test
	public void testSelectListMembers() {
		List<Member> chosenEmails = selectListMembers(data, m->m.getEmail().equals("foo@bar.com"));
		assertEquals(1, chosenEmails.size());
		assertSame(data.get(0).getEmail(), chosenEmails.get(0).getEmail());
	}
}
