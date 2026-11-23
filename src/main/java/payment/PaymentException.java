package payment;

/** A payment could not be started, or could not be verified. The message is safe to log. */
public class PaymentException extends Exception {

	private static final long serialVersionUID = 1L;

	public PaymentException(String message) {
		super(message);
	}

	public PaymentException(String message, Throwable cause) {
		super(message, cause);
	}
}
