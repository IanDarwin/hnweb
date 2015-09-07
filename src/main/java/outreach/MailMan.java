package outreach;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import data.PersonList;
import model.Member;

@ManagedBean(name="mailMan") @RequestScoped
public class MailMan implements Serializable {

	private static final String PROPNAME_MAIL_SMTP_HOST = "mail.smtp.host";

	private static final String CONFIG_PROPERTIES_NAME = "/config.properties";

	private static final long serialVersionUID = -1980670543127065573L;

	@Inject
	PersonList lister;
	
	@Resource(mappedName="java:jboss/mail/Default")
	Session session;		// non-private only for testing.
	
	String subject;
	String messageBody;
	
	static final Properties props = 
		propsFromClassPath(MailMan.class, CONFIG_PROPERTIES_NAME);
	
	public MailMan() {
		System.out.println("MailMan.MailMan()");
	}
	
	private static Properties propsFromClassPath(Class<?> clazz, String propsName) {
		try (InputStream is = clazz.getResourceAsStream(propsName)) {
			if (is == null) {
				throw new IllegalArgumentException(
					"File " + propsName + " not on CLASSPATH for " + clazz.getName());
			}
			Properties p = new Properties();
			p.load(is);
			p.store(System.out, "Meh");
			return p;
		} catch (IOException e) {
			throw new IllegalStateException(
				"CANTHAPPEN: IOException in close of resource " + propsName);
		}
	}

	public void sendMessageToAll() throws MessagingException {
		System.out.println("MailMan.sendMessageToAll()");
		sendMessageToSome(null);
	}
	
	public void sendMessageToSome(Chooser ch) throws MessagingException {
		System.out.println("MailMan.sendMessageToSome()");
		List<Member> all = lister.findAll();
		List<String> emails = selectListMembers(all, ch);
		final String mailHostProperty = props.getProperty(PROPNAME_MAIL_SMTP_HOST);
		if (mailHostProperty == null) {
			throw new IllegalStateException(
				PROPNAME_MAIL_SMTP_HOST + " not set in " + CONFIG_PROPERTIES_NAME);
		}
		sendMessage(props.getProperty("mail.sender"),
				emails, 
				subject, messageBody);
	}

	/**
	 * Perform list subsetting
	 * @param list The list of members
	 * @param ch The chooser
	 * @return The subset list
	 */
	public static List<String> selectListMembers(List<Member> list, Chooser ch) {
		List<String> emails = new ArrayList<>(list.size());
		for (Member p : list) {
			final String email = p.getEmail();
			if (email == null) {
				continue;
			}
			if (ch != null && !ch.accept(p)) {
				continue;
			}
			emails.add(email);
		}
		return emails;
	}

	private void sendMessage(
			String fromAddress, List<String> emails, 
			String message_subject, String message_body) throws MessagingException {
		System.out.println("MailMan.sendMessage()");
		
		notNull("fromAddress", fromAddress);
		notNull("message_subject", message_subject);
		notNull("message_body", message_body);
		
		// Create the Session object
		if (session == null) {
			throw new NullPointerException("Mail Session not injected; check configuration");
		}

		// create a message
		Message mesg = new MimeMessage(session);

		// From Address
		final InternetAddress fromInternetAddress = new InternetAddress(fromAddress);
		mesg.setFrom(fromInternetAddress);
		// Send ourself a copy as well CC Address
		mesg.addRecipient(Message.RecipientType.BCC, fromInternetAddress);

		// TO Addresses - may be a lot!
		for (String message_recip : emails) {
			InternetAddress toAddress = new InternetAddress(message_recip);
			mesg.addRecipient(Message.RecipientType.TO, toAddress);
		}

		// The Subject
		mesg.setSubject(message_subject);

		// Now the message body.
		mesg.setText(message_body);

		// Finally, send the message!
		Transport.send(mesg);
	}

	private void notNull(String name, String value) {
		if (value == null) {
			throw new NullPointerException("Value of " + name + " must not be null!");
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
}
