package outreach;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import action.LoginAction;
import data.MemberList;
import model.Member;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MailManTest {

	private static final String HTML_HELLO_WORLD = "<html><p>Hello";
	Stream<Member> fakeList = Stream.of(
			new Member("Tom", null, "Jones", "devnull@ojwkjsflkjew.com")
	);
	final String A_MESSAGE_SUBJECT = "About your night life";
	private MailMan sender;
	private Member member;
	private LoginAction identity;
	
	@Before
	public void init() {
		MemberList lister = mock(MemberList.class);
		when(lister.findAll()).thenReturn(fakeList);
		sender = new MailMan();
		sender.lister = lister;
		member = new Member("Test", "T.", "Test", "noreply@nobody.home");
		identity = new LoginAction();
		identity.setLoggedInUser(member);
		MailMan.props.clear();
	}
	
	@Test
	public void testCreateMessageGetBody() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final MimeMessage actual = sender.createMessage("from@dom.ain",
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		Multipart content = (Multipart) actual.getContent();
		for (int i = 0; i < content.getCount(); i++) {
			if (HTML_HELLO_WORLD.equals(content.getBodyPart(i).getContent())) {
				return;
			}
		}
		fail("get content matching " + HTML_HELLO_WORLD);
	}
	
	@Test
	public void testCreateMessage() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final String messageSender = "from@dom.ain";
		final MimeMessage actual = sender.createMessage(messageSender,
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals("Subject", A_MESSAGE_SUBJECT, actual.getSubject());
		assertEquals("From", new InternetAddress(messageSender), actual.getFrom()[0]);
	}

	@Test
	public void testCreateMessageWithNoReply() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final String NO_REPLY_ADDR = "slugs@junglegarden.moc";
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_SMTP_FROM, NO_REPLY_ADDR);
		final MimeMessage actual = sender.createMessage("from@dom.ain",
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals("Subject", A_MESSAGE_SUBJECT, actual.getSubject());
		assertEquals("From", new InternetAddress(NO_REPLY_ADDR), actual.getFrom()[0]);
		assertEquals("Reply-to", new InternetAddress("from@dom.ain"), actual.getReplyTo()[0]);
	}
}
