package outreach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.Member;

public class ChooserTest {

	final List<Member> data = new ArrayList<>();
	final Member m1 = new Member();
	final Member m2 = new Member();
	final Member m3 = new Member();
	final Member m4 = new Member();

	@Before
	public void setup() {
		data.clear();
		m1.setEmail("foo@bar.com");
		data.add(m1);
		m2.setEmail("nobody@all");
		data.add(m2);
		m3.setEmail("donald@duckduckgo");
		data.add(m3);
		m4.setEmail("no@no");
		data.add(m4);
	}

	@Test
	public void testSelectListMembers() {
		List<String> chosenEmails = MailMan.selectListMembers(data, m->m.getEmail().equals("foo@bar.com"));
		assertEquals(1, chosenEmails.size());
		assertSame(data.get(0).getEmail(), chosenEmails.get(0));
	}
	
	/** This has to be just right:
	 * 1) If it is now December, and you expired in September, you should still get the newsletter.
	 * 2) If it is now January, and you expired in September, you should still get the newsletter
	 * 3) If it is now January and you expired 15 months ago, you should NOT get the newsletter
	 */
	@Test
	public void testPaidMemberChooser() {
		MailMan.currentCalendarYear = 2020;
		Member m = new Member("Jo", "J.", "Schmoe", "jo@schmo.net");
		List<Member> list = Arrays.asList(new Member[] {m});
		List<String> emails;
		
		// Case 1
		m.setExpiryYear(2020);
		emails = MailMan.selectListMembers(list, MailMan.paidMemberChooser);
		assertEquals(1, emails.size());

		// Case 2
		m.setExpiryYear(2019);
		emails = MailMan.selectListMembers(list, MailMan.paidMemberChooser);
		assertEquals(1, emails.size());

		// Case 3
		m.setExpiryYear(2018);
		emails = MailMan.selectListMembers(list, MailMan.paidMemberChooser);
		assertEquals(0, emails.size());
	}
}
