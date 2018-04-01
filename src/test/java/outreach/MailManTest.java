package outreach;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import action.LoginAction;
import data.MemberList;
import model.Member;

@RunWith(PowerMockRunner.class)
public class MailManTest {

	List<Member> fakeList = Arrays.asList(
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
	}
	
	@Test
	@PrepareForTest({Transport.class,FacesMessage.class})
	public void testSendMessageToAll() throws Exception {

		//final Properties properties = mockSession.getProperties();
		PowerMockito.mockStatic(Transport.class);
		Properties props = MailMan.props;
		sender.identity = identity;
		sender.session = 
				// PowerMockito.mock(Session.class);
				Session.getDefaultInstance(props);

		sender.subject = "This is a test message";
		sender.messageBody = "This is a test message body";
		sender.sendMessageToAll();
		
		PowerMockito.verifyStatic();

	}
	
	@Test
	public void testCreateMessage() throws Exception {
		List<String> recipients = Arrays.asList(new String[]{"a@b.c"});
		final String messageSender = "from@dom.ain";
		final MimeMessage actual = sender.createMessage(messageSender,
				A_MESSAGE_SUBJECT,
				"<html><p>Hello",
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals("Subject", A_MESSAGE_SUBJECT, actual.getSubject());
		assertEquals("From", new InternetAddress(messageSender), actual.getFrom()[0]);
	}

	@Test
	public void testCreateMessageWithNoReply() throws Exception {
		List<String> recipients = Arrays.asList(new String[]{"a@b.c"});
		final String NO_REPLY_ADDR = "slugs@junglegarden.moc";
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_NO_REPLY, NO_REPLY_ADDR);
		final MimeMessage actual = sender.createMessage("from@dom.ain",
				A_MESSAGE_SUBJECT,
				"<html><p>Hello",
				recipients,
				null, null);
		// XXX how to get the body parts?? assertEquals("Hello", actual.getXXX.getText()));
		assertEquals("Subject", A_MESSAGE_SUBJECT, actual.getSubject());
		assertEquals("From", new InternetAddress(NO_REPLY_ADDR), actual.getFrom()[0]);
	}

	@Test
	public void testsanitizeToPlainText() {
		String htmlBody = "<span style='font-size: 12px; font-style: italic;'>Time</span><span style='font-size: 12px;'> is <span style='font-weight: bold;'>not</span> enough.</span>";
		final String actual = sender.sanitizeToPlainText(htmlBody);
		assertEquals("Sanitize to plain text", "Time is not enough.", actual);
	}
}
