package outreach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.Member;

public class ChooserTest {

	List<Member> data;

	@Before
	public void setup() {
		data = new ArrayList<>();
		final Member m = new Member();
		m.setEmail("foo@bar.com");
		data.add(m);
		final Member m2 = new Member();
		m2.setEmail("nobody@all");
		data.add(m2);
	}

	@Test
	public void testSelectListMembers() {
		List<String> chosenEmails = MailMan.selectListMembers(data, m->m.getEmail().equals("foo@bar.com"));
		assertEquals(1, chosenEmails.size());
		assertSame(data.get(0).getEmail(), chosenEmails.get(0));
	}
}
