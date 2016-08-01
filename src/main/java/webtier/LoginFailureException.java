package webtier;

public class LoginFailureException extends RuntimeException { 

	private static final long serialVersionUID = 1L;

	public LoginFailureException() {
		super();
	}

	public LoginFailureException(String mesg) {
		super(mesg);
	}

	public LoginFailureException(String mesg, Throwable cause) {
		super(mesg, cause);
	}
}
