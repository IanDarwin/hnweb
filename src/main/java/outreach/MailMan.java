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
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.Address;
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

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

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
 * Has to be session scoped so the upload doesn't get done in a separate
 * instance than the submit, so beware of field-reuse issues.
 * XXX consider conversation scope?
 */
@ManagedBean(name="mailMan") @SessionScoped
public class MailMan implements Serializable {

	private static final String PROPNAME_MAIL_SMTP_HOST = "mail.smtp.host";

	private static final String CONFIG_PROPERTIES_NAME = "/config.properties";

	private static final long serialVersionUID = -1980670543127065573L;

	@Inject
	MemberList lister;
	@ManagedProperty("#{identity}")
	LoginAction identity;
	
	@Resource(mappedName="java:jboss/mail/Default")
	protected /*Mail*/Session session;		// Non-private only for testing.
	
	protected String subject;				// Ditto
	protected String messageBody;

	// For uploaded attachment
	private String uploadFileName;
	private byte[] uploadData = null;
	private String uploadContentType = null;

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
		if (sendMessage(Arrays.asList(identity.getLoggedInUser().getEmail()), subject, messageBody)) {
			addFacesMessage("Message sent to your email, please proof-read.");
		}
	}

	/**
	 * Send an email to everybody
	 */
	public String sendMessageToAll() throws MessagingException {
		System.out.println("MailMan.sendMessageToAll()");
		if (sendMessageToSome(null)) {
			addFacesMessage("Message sent to entire list.");
		}
		return "/";
	}

	/**
	 * Send an email to the executive only.
	 */
	public void sendMessageToExec() throws MessagingException {
		System.out.println("MailMan.sendMessageToExec()");
		if (sendMessageToSome(m -> m.getPosition() != null && m.getPosition().length() > 0)) {
			addFacesMessage("Message sent to Exec List.");
		}
	}

	/**
	 * Send a dunning letter to members that have not renewed.
	 */
	public void sendMessageToExpired() throws MessagingException {
		System.out.println("MailMan.sendMessageToExpired()");
		LocalDate d = LocalDate.now();
		int year = d.getYear();
		if (sendMessageToSome(m -> m.getExpiryYear() < year)) {
			addFacesMessage("Message sent to expired members.");
		}
	}

	/**
	 * Common code for Some of the sendMessageToXXX() methods;
	 * takes a lambda for including/excluding each member from the list.
	 */
	public boolean sendMessageToSome(Chooser ch) throws MessagingException {
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

		if (true) {
			String from = mesg.getFrom()[0].toString();
			String reply = mesg.getHeader("Reply-to")[0];
			StringBuffer to = new StringBuffer();
			for (Address recip : mesg.getAllRecipients()) {
				to.append(recip).append(',');
			}
			System.out.printf("From %s, Reply %s, To %s\n", from, reply, to.toString());
		}
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
	 * Create the javax.mail.Message object to be sent.
	 * 
	 * @param fromAddress - the (typically fake) email
	 * @param messageSubject The subject
	 * @param htmlBody The body, as plain text only for now
	 * @param recipients a List of String email addresses
	 * @param uploadData2 
	 * @param uploadContentType2 
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

		// From Address: send with two headers:
		// From: noreply@darwinsys.com
		// Reply-to: $user.email
		String listAddress = "noreply@darwinsys.com";
		final InternetAddress fromInternetAddress = new InternetAddress(listAddress);
		mesg.setFrom(fromInternetAddress);
		mesg.addHeader("Reply-to", fromAddress);
		
		// Send ourself a copy as the (only) 'To' address
		mesg.addRecipient(Message.RecipientType.TO, fromInternetAddress);

		// TO Addresses - may be a lot! But send as BCC as usual for privacy.
		for (String message_recip : recipients) {
			InternetAddress toAddress = new InternetAddress(message_recip);
			mesg.addRecipient(Message.RecipientType.BCC, toAddress);
		}

		// The Subject
		mesg.setSubject(messageSubject);

        // Now the message body, in HTML and in plain text
        Multipart mp = new MimeMultipart();
        
        // Message comes in HTML from the JSF Editor pane
        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html");
        mp.addBodyPart(htmlPart);

        // Convert to plain text and send it that way too, for olde mailers.
        BodyPart textPart = new MimeBodyPart();
        String plainBody = sanitizeToPlainText(htmlBody);
        textPart.setText(plainBody); // sets type of this part to "text/plain"
        mp.addBodyPart(textPart);
        
        // Toss in one attachment if there is one
        if (uploadContentType != null) {
        		BodyPart attachPart = new MimeBodyPart();
        		attachPart.setContent(uploadData, uploadContentType);
			attachPart.setFileName(uploadFileName);
        		mp.addBodyPart(attachPart);
        }

        // Put the MultiPart into the Message
        mesg.setContent(mp);
		return mesg;
	}

	/** Convert a string containing an HTML message into plain text */
	protected String sanitizeToPlainText(String htmlBody) {
		final String rendered = new HtmlToPlainText().getPlainText(Jsoup.parse(htmlBody, "ISO-8859-1"));
		// For some braindead reason JSoup thinks spaces should print as char '160' not char '40'...
		return rendered.replace('\u00a0', ' ');
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
        uploadData = file.getContents();
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
