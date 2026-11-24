package outreach;

public class MailSendException extends RuntimeException {
    private static final long serialVersionUID = 1234567L;
	public MailSendException(String message, Throwable cause) {
		super(message, cause);
	}
}
