package outreach;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import jakarta.annotation.Resource;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import action.LoginAction;
import data.MemberDAO;
import model.Member;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * Sends email on behalf of the club.
 * The "mail.smtphost" property must be set.
 * The PROPNAME_MAIL_NO_REPLY property may be set to a fake "no-reply" type address,
 * absent which the mail will be sent from the email of the logged-in user,
 * assuming that this is syntactically valid (failing that, an exception
 * will be thrown in the mailing process, and you don't want that).
 * Has to be session scoped so that the upload doesn't get done in a separate
 * instance than the submit, so beware of field-reuse issues.
 * XXX consider conversation scope?
 */
@Named("mailMan") @SessionScoped
public class MailMan implements Serializable {

	private static final String HEADER_REPLY_TO = "Reply-To";

	public static final String CONFIG_PROPERTIES_NAME = "/config.properties";

	public static final String PROPNAME_MAIL_SMTP_HOST = "mail.smtp.host";

	public static final String PROPNAME_MAIL_SMTP_FROM = "mail.smtp.from";

	public static final String PROPNAME_MAIL_SUBJECT_PREFIX = "mail.subject.prefix";

	public static final String PROPNAME_MAIL_UNSUBSCRIBE = "mail.unsubscribe";

	@Serial
	private static final long serialVersionUID = -1980670543127065573L;

	@Inject
	MemberDAO lister;

	@Inject @Named("identity")
	LoginAction identity;
	
	@Resource(mappedName="java:jboss/mail/Default")
	protected /*Mail*/Session session;		// Non-private only for testing.
	
	protected String subject;				// Ditto
	protected String messageBody;

	// For uploaded attachment
	private String uploadFileName;
	private byte[] uploadData = null;
	private String uploadContentType = null;

	/** This is non-final to allow mocking */
	static LocalDate now = LocalDate.now();

	public MailMan() {
		System.out.println("MailMan.MailMan()");
	}

	static final Properties props;

	static {
		try (InputStream is = MailMan.class.getResourceAsStream(CONFIG_PROPERTIES_NAME)) {
			if (is == null) {
				throw new IllegalArgumentException(
					"File " + CONFIG_PROPERTIES_NAME + " not on CLASSPATH for " +
							MailMan.class.getName());
			}
			props = new Properties();
			props.load(is);
			props.store(System.out, "Dump of MailMan config properties");
		} catch (IOException e) {
			throw new ExceptionInInitializerError(
				"CANTHAPPEN: IOException in close of resource " + CONFIG_PROPERTIES_NAME);
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
		if (sendMessage(Collections.singletonList(identity.getLoggedInUser().getEmail()), subject, messageBody)) {
			addFacesMessage("Message sent to your email, please proof-read.");
		}
		// Must stay on same page here!
	}

	/**
	 * Send an email to everybody
	 */
	public String sendMessageToAll() throws MessagingException {
		System.out.println("MailMan.sendMessageToAll()");
		final int numSent = sendMessageToSome(null);
		if (numSent > 0) {
			addFacesMessage(
				String.format("Message sent to entire list - %d current and former members.", numSent));
			return "/";
		} else {
			addFacesMessage("Unable to send message!");
			return null;
		}
	}

	/**
	 * Send an email to the executive only.
	 */
	public String sendMessageToExec() throws MessagingException {
		System.out.println("MailMan.sendMessageToExec()");
		int numSent = sendMessageToSome(m -> m.getPosition() != null && m.getPosition().length() > 0);
		if (numSent > 0) {
			addFacesMessage(String.format("Message sent to %d Exec Members.", numSent));
			return "/";
		} else {
			addFacesMessage("Unable to send message!");
			return null; // stay on page to try to fix it??
		}
	}
	
	/**
	 * Send an email to one address only.
	 */
	public String sendMessageTo(String email, String subject, String body) throws MessagingException {
		System.out.println("MailMan.sendMessageTo(" + email + ")");
		var ret = sendMessage(List.of(email), subject, body);
		if (ret) {
			return "/";
		} else {
			addFacesMessage("Unable to send message!");
			return null;
		}
	}
	

	/** This has to be just right:
	 * If it is now December, and you expired in September, you are not paid up.
	 * If it is now January, and you expired in September, are paid up.
	 * If it is now January and you expired 15 months ago, you should NOT be considered paid up.
	 */
	final static Predicate<Member> paidMemberChooser = member -> {
		LocalDate memberExpiry = LocalDate.of(member.getExpiryYear(), Month.AUGUST, 31);
		return now.isBefore(memberExpiry);
	};

	/**
	 * Send an email to the paid-up membership only.
	 */
	public String sendMessageToPaidUp() throws MessagingException {
		System.out.println("MailMan.sendMessageToPaidUp()");
		int numSent = sendMessageToSome(paidMemberChooser);
		if (numSent > 0) {
			addFacesMessage(String.format("Message sent to %d Paid-up Members.", numSent));
			return "/";
		} else {
			addFacesMessage("Unable to send message!");
			return null;
		}
	}

	/**
	 * Send a dunning letter to members that have not renewed.
	 */
	public String sendMessageToExpired() throws MessagingException {
		System.out.println("MailMan.sendMessageToExpired()");
		LocalDate d = LocalDate.now();
		int year = d.getYear();
		final int numSent = sendMessageToSome(m -> m.getExpiryYear() < year);
		if (numSent > 0) {
			addFacesMessage(String.format("Message sent to %d expired members.", numSent));
			return "/";
		} else {
			addFacesMessage("Did not send message to anybody!");
			return null;
		}
	}

	/**
	 * Common code for the sendMessageToXXX() methods;
	 * takes a lambda for including/excluding each member from the list.
	 */
	int sendMessageToSome(Predicate<Member> ch) throws MessagingException {
		System.out.println("MailMan.sendMessageToSome()");
		List<Member> all = lister.findAll();
		List<String> emails = selectListMembers(all, ch);
		final String mailHostProperty = props.getProperty(PROPNAME_MAIL_SMTP_HOST);
		if (mailHostProperty == null) {
			throw new IllegalStateException(
				PROPNAME_MAIL_SMTP_HOST + " not set in " + CONFIG_PROPERTIES_NAME);
		}
		return sendMessage(
				emails, 
				subject, messageBody) ? emails.size() : 0;
	}

	/**
	 * Perform list subsetting
	 * @param list The list of members
	 * @param ch The Predicate
	 * @return The subset list, as email strings
	 */
	static List<String> selectListMembers(List<Member> list, Predicate<Member> ch) {
		List<String> emails = new ArrayList<>(list.size());
		for (Member p : list) {
			final String email = p.getEmail();
			if (email == null || email.isEmpty()) {
				continue;
			}
			if (ch != null && !ch.test(p)) {
				continue;
			}
			emails.add(email);
		}
		return emails;
	}

	/** 
	 * Send the message.
	 */
	private boolean sendMessage(
			List<String> emails, 
			String message_subject, String message_body) throws MessagingException {
		System.out.println("MailMan.sendMessage()");
		
		Member loggedIn = identity.getLoggedInUser();
		notNull("your email address", loggedIn.getName());
		String fromAddress = loggedIn.getName() + " <" + loggedIn.getEmail() + ">";
		notNull("message_subject", message_subject);
		notNull("message_body", message_body);
		
		// Get the Mail Session object
		if (session == null) {
			throw new NullPointerException("Mail Session not injected; check configuration");
		}

		MimeMessage mesg = createMessage(fromAddress, message_subject, message_body, emails,
				uploadContentType, uploadData);

			String from = mesg.getFrom()[0].toString();
			final String[] toHeaders = mesg.getHeader(HEADER_REPLY_TO);
			String reply = toHeaders != null ? toHeaders[0] : "(unset)";
			StringBuilder to = new StringBuilder();
			for (Address recip : mesg.getAllRecipients()) {
				to.append(recip).append(',');
			}
			System.out.printf("MailMan: From %s, %s %s, To %s\n", from, HEADER_REPLY_TO, reply, to);

		// Finally, send the message!
		try {
			Transport.send(mesg);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			addFacesMessage("Final sending step failed, probably unable to connect to mail server");
			return false;
		}
	}

	/** 
	 * Create the jakarta.mail.Message object to be sent.
	 * 
	 * @param fromAddress - the (typically fake) email
	 * @param messageSubject The subject
	 * @param htmlBody The body, as plain text only for now
	 * @param recipients a List of String email addresses
	 * @param uploadContentType  The content type
	 * @return The mail message
	 * @throws MessagingException If the message format isn't quite right
	 */
	protected MimeMessage createMessage(String fromAddress, 
			String messageSubject, 
			String htmlBody,
			List<String> recipients,
			String uploadContentType,
			byte[] uploadData) throws MessagingException {
		// create a message
		MimeMessage mesg = new MimeMessage(session);

		String noReplyAddress = props.getProperty(PROPNAME_MAIL_SMTP_FROM);
		if (noReplyAddress == null) {	// Simple mode
			final InternetAddress fromInternetAddress = new InternetAddress(fromAddress);
			mesg.setFrom(fromInternetAddress);
		} else {
			// In "no-reply" mode, for From Address: send with two headers:
			// From: noreply@whatever.dom
			// Reply-to: $user.email
			mesg.setFrom(new InternetAddress(noReplyAddress));
			mesg.setReplyTo(new Address[] {new InternetAddress(fromAddress)});
		}

		// Additional list headers
		mesg.addHeader("List-Id", "Headwaters Nature <hn.darwinsys.com>");
		// Needed to get past Google, Yahoo/Rogers, etc.:
		mesg.addHeader("List-Unsubscribe", "https://darwinsys.com/unsub/hn");
		mesg.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

		// Send yourself a copy as the (only) 'To' address
		mesg.addRecipient(Message.RecipientType.TO, new InternetAddress(fromAddress));

		// TO Addresses - may be a lot! But send as BCC as usual for privacy.
		for (String message_recip : recipients) {
			InternetAddress toAddress = new InternetAddress(message_recip);
			mesg.addRecipient(Message.RecipientType.BCC, toAddress);
		}

		// The Subject
		String prefix = props.getProperty(PROPNAME_MAIL_SUBJECT_PREFIX);
		if (prefix != null) {
			messageSubject = prefix + " " + messageSubject;
		}
		mesg.setSubject(sanitizeToUtf8(messageSubject));

        // Now the message body, in HTML and in plain text
        Multipart mp = new MimeMultipart();
        
        // Message comes in HTML from the JSF Editor pane
        BodyPart htmlPart = new MimeBodyPart();
		// Will often have been pasted from some kinda WP software w/ smartquotes &c
		htmlBody += "<p>Unsubscribe: " +
			props.getProperty(PROPNAME_MAIL_UNSUBSCRIBE);
        htmlPart.setContent(sanitizeToUtf8(htmlBody), "text/html");
		htmlPart.addHeader("Content-Disposition", "inline");
        mp.addBodyPart(htmlPart);
        
        // Toss in one attachment if there is one
        if (uploadContentType != null) {
        		BodyPart attachPart = new MimeBodyPart();
        		attachPart.setContent(uploadData, uploadContentType);
			attachPart.setFileName(uploadFileName);
        		mp.addBodyPart(attachPart);
        }

        // Put the MultiPart into the Message
        mesg.setContent(mp);
        System.out.println("outreach.MailMan: " + mesg.getHeader("Content-Type"));
		//mesg.setContentType("multipart/alternative");
		return mesg;
	}

	/** For the HTML sending version, get rid of janky special characters that break badly in this stack */
	static String sanitizeToHtml(String s) {
		return s
				.replaceAll("'","\\&apos;")  // Apostrophe into HTML,prob. unnecessary
				.replaceAll("\u2014","\\&mdash;")  // "Smart" em dash
				.replaceAll("\u8212","\\&mdash;")  // Another "Smart" em dash
				.replaceAll("\u2018","\\&apos;") // "Smart" open single quote
				.replaceAll("\u2019","\\&apos;") // "Smart" close single quote
				.replaceAll("\u201c","\\&quot;") // "Smart" open dquote
				.replaceAll("\u201d","\\&quot;") // "Smart" close dquote
				;
	}

	static String sanitizeToUtf8(String s) {
		return s
				.replaceAll("\u2014","--") // "Smart" em dash
				.replaceAll("\u2018","\'") // "Smart" open quote
				.replaceAll("\u2019","\'") // "Smart" close quote
				.replaceAll("\u201c","\"") // "Smart" open dquote
				.replaceAll("\u201d","\"") // "Smart" close dquote
				;
	}

	private void notNull(String name, String value) {
		if (value == null) {
			throw new NullPointerException("Value of " + name + " must not be null!");
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		System.out.println("MailMan.handleFileUpload()");
        final UploadedFile file = event.getFile();
        uploadFileName = file.getFileName();
        uploadData = file.getContent();
        uploadContentType = file.getContentType();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Succesful", file.getFileName() + " is uploaded."));
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
