package outreach;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import action.LoginAction;
import data.MemberList;
import model.Member;

/**
 * Sends email on behalf of the club.
 * The "mail.smtphost" property must be set.
 * The "mail.sender" property may be set to a fake "no-reply" type address,
 * absent which the mail will be sent from the email of the logged-in user,
 * assuming that this is syntactically valid (failing that, an exception
 * will be thrown in the mailing process, and you don't want that).
 */
@ManagedBean(name="mailMan") @RequestScoped
public class MailMan implements Serializable {

	private static final String PROPNAME_MAIL_SMTP_HOST = "mail.smtp.host";

	private static final String CONFIG_PROPERTIES_NAME = "/config.properties";

	private static final long serialVersionUID = -1980670543127065573L;

	@Inject
	MemberList lister;
	@ManagedProperty("#{identity}")
	LoginAction identity;
	
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
	
	protected void addFacesMessage(String message) {
		final FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			// FacesContext will be null in simple JUnit testing...
			System.out.println("FacesMessage is null! Losing message: " + message);
			return;			// cheaper than an NPE
		}
		final FacesMessage facesMessage = new FacesMessage(message);
		facesContext.addMessage(null, facesMessage);
	}

	/**
	 * Send a message to yourself, typically to test how a message will look before sending to a group.
	 */
	public void sendMessageToMe() throws MessagingException {
		System.out.println("MailMan.sendMessageToMe()");
		if (identity.getLoggedInUser() == null) {
			addFacesMessage("Are you sure you logged in?");
			return;
		}
		sendMessage(
                Arrays.asList(identity.getLoggedInUser().getEmail()),
                subject, messageBody);
		addFacesMessage("Message sent to your email, please proof-read.");
	}

	/**
	 * Send an email to everybody
	 */
	public void sendMessageToAll() throws MessagingException {
		System.out.println("MailMan.sendMessageToAll()");
		sendMessageToSome(null);
		addFacesMessage("Message sent to entire list.");
	}

	/**
	 * Send an email to the executive only.
	 */
	public void sendMessageToExec() throws MessagingException {
		System.out.println("MailMan.sendMessageToExec()");
		sendMessageToSome(m -> m.getPosition() != null);
		addFacesMessage("Message sent to Exec List.");
	}

	/**
	 * Send a dunning letter to members that have not renewed.
	 */
	public void sendMessageToExpired() throws MessagingException {
		System.out.println("MailMan.sendMessageToExpired()");
		LocalDate d = LocalDate.now();
		int year = d.getYear();
		sendMessageToSome(m -> m.getExpiryYear() < year);
		addFacesMessage("Message sent to expired members.");
	}

	/**
	 * Common code for Some of the sendMessageToXXX() methods;
	 * takes a lambda for including/excluding each member from the list.
	 */
	public void sendMessageToSome(Chooser ch) throws MessagingException {
		System.out.println("MailMan.sendMessageToSome()");
		List<Member> all = lister.findAll();
		List<String> emails = selectListMembers(all, ch);
		final String mailHostProperty = props.getProperty(PROPNAME_MAIL_SMTP_HOST);
		if (mailHostProperty == null) {
			throw new IllegalStateException(
				PROPNAME_MAIL_SMTP_HOST + " not set in " + CONFIG_PROPERTIES_NAME);
		}
		sendMessage(
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
			if (email == null || email.isEmpty()) {
				continue;
			}
			if (ch != null && !ch.accept(p)) {
				continue;
			}
			emails.add(email);
		}
		return emails;
	}

	/** 
	 * Send the message. The "from" address is either the mail.sender property or the logged-in user's own.
	 */
	private void sendMessage(
			List<String> emails, 
			String message_subject, String message_body) throws MessagingException {
		System.out.println("MailMan.sendMessage()");
		
		Member loggedIn = identity.getLoggedInUser();
		String fromAddress = props.getProperty("mail.sender") != null ?
			props.getProperty("mail.sender") :
			loggedIn.getName() + " <" + loggedIn.getEmail() + ">";
		notNull("message_subject", message_subject);
		notNull("message_body", message_body);
		
		// Create the Session object
		if (session == null) {
			throw new NullPointerException("Mail Session not injected; check configuration");
		}

		Message mesg = createMessage(fromAddress, message_subject, message_body, emails);

		// Finally, send the message!
		Transport.send(mesg);
	}

	/** 
	 * Create the javax.mail.Message object to be sent.
	 * 
	 * @param fromAddress - the (typically fake) email
	 * @param messageSubject The subject
	 * @param messageBody The body, as plain text only for now
	 * @param recipients a List of String email addresses
	 * @return The mail message
	 * @throws MessagingException If the message format isn't quite right
	 */
	protected Message createMessage(String fromAddress, 
			String messageSubject, 
			String messageBody,
			List<String> recipients) throws MessagingException {
		// create a message
		Message mesg = new MimeMessage(session);

		// From Address
		final InternetAddress fromInternetAddress = new InternetAddress(fromAddress);
		mesg.setFrom(fromInternetAddress);
		// Send ourself a copy as well as 'To' address
		mesg.addRecipient(Message.RecipientType.TO, fromInternetAddress);

		// TO Addresses - may be a lot! Send as BCC as usual for privacy.
		for (String message_recip : recipients) {
			InternetAddress toAddress = new InternetAddress(message_recip);
			mesg.addRecipient(Message.RecipientType.BCC, toAddress);
		}

		// The Subject
		mesg.setSubject(messageSubject);

        // Now the message body.
        Multipart mp = new MimeMultipart();

        // BodyPart textPart = new MimeBodyPart();
        // We could use TagSoup to convert to plain text I suppose?
        // textPart.setText(message_body); // sets type to "text/plain"
        // mp.addBodyPart(textPart);

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(messageBody, "text/html");
        mp.addBodyPart(htmlPart);

        // Put the MultiPart into the Message
        mesg.setContent(mp);
		return mesg;
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

	public LoginAction getIdentity() {
		return identity;
	}

	public void setIdentity(LoginAction identity) {
		this.identity = identity;
	}
}
