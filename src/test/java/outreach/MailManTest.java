package outreach;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.mail.Session;
import javax.mail.Transport;

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
			// XXX Grab the Sender email from the config.props and use that!
			new Member("Tom", null, "Jones", "devnull@ojwkjsflkjew.com")
	);
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
	public void test() throws Exception {

		//final Properties properties = mockSession.getProperties();
		PowerMockito.mockStatic(Transport.class);
		Properties props = MailMan.props;
		sender.identity = identity;
		sender.session = // PowerMockito.mock(Session.class);
				Session.getDefaultInstance(props);

		sender.subject = "This is a test message";
		sender.messageBody = "This is a test message body";
		sender.sendMessageToAll();
		
		PowerMockito.verifyStatic();

	}

}
