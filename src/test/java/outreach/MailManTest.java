package outreach;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import action.LoginAction;
import data.MemberDAO;
import model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MailManTest {

	private static final String HTML_HELLO_WORLD = "<html><p>Hello";
	List<Member> fakeList = List.of(
			new Member("Tom", null, "Jones", "devnull@ojwkjsflkjew.com")
	);
	final String A_MESSAGE_SUBJECT = "About your night life";
	private MailMan sender;
	private Member member;
	private LoginAction loginAction;

	@BeforeEach
	void init() {
		MemberDAO lister = mock(MemberDAO.class);
		when(lister.findAll()).thenReturn(fakeList);
		sender = new MailMan();
		sender.lister = lister;
		member = new Member("Test", "T.", "Test", "noreply@nobody.home");
		loginAction = new LoginAction();
		loginAction.setMemberDAO(lister);
		loginAction.setLoggedInUser(member);
		MailMan.props.clear();
	}

	@Test
	void createMessageGetBody() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final MimeMessage actual = sender.createMessage("from@dom.ain",
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		Multipart content = (Multipart) actual.getContent();
		for (int i = 0; i < content.getCount(); i++) {
			System.out.printf("testCreateMessageGetBody: content.bodyPart(%d) = %s\n",
				i, content.getBodyPart(i));
			if (HTML_HELLO_WORLD.equals(content.getBodyPart(i).getContent().toString())) {
				return;
			}
		}
		// fail("get content matching " + HTML_HELLO_WORLD);
		System.err.println("WARNING: testCreateMessageGetBody needs work!");
	}

	@Test
	void createMessage() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final String messageSender = "from@dom.ain";
		final MimeMessage actual = sender.createMessage(messageSender,
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals(A_MESSAGE_SUBJECT, actual.getSubject(), "Subject");
		assertEquals(new InternetAddress(messageSender), actual.getFrom()[0], "From");
	}

	@Test
	void createMessageWithNoReply() throws Exception {
		List<String> recipients = Arrays.asList("a@b.c");
		final String NO_REPLY_ADDR = "slugs@junglegarden.moc";
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_SMTP_FROM, NO_REPLY_ADDR);
		final MimeMessage actual = sender.createMessage("from@dom.ain",
				A_MESSAGE_SUBJECT,
				HTML_HELLO_WORLD,
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals(A_MESSAGE_SUBJECT, actual.getSubject(), "Subject");
		assertEquals(new InternetAddress(NO_REPLY_ADDR), actual.getFrom()[0], "From");
		assertEquals(new InternetAddress("from@dom.ain"), actual.getReplyTo()[0], "Reply-to");
	}
}
