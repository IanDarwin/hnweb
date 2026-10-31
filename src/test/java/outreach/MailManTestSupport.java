package outreach;

import jakarta.mail.Session;

/**
 * Test-only bridge into package {@code outreach}: MailMan's static {@code props} and its
 * {@code session} field aren't visible to tests in other packages.
 */
public class MailManTestSupport {

	public static final String FROM = "noreply@hn.test";

	/**
	 * A real MailMan (not a mock) that sends through the given Session,
	 * e.g. one pointed at a GreenMail SMTP server.
	 */
	public static MailMan mailManUsing(Session mailSession) {
		// MailMan.props is static and shared; other tests (MailManTest) clear it,
		// so set what we need each time.
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_SMTP_FROM, FROM);
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_SUBJECT_PREFIX, "[HN-test]");
		MailMan.props.setProperty(MailMan.PROPNAME_MAIL_UNSUBSCRIBE, "n/a");
		return new MailMan() {
			{ this.session = mailSession; }	// protected field: legal in a subclass
		};
	}
}
