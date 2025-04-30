package action;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpSession;

import com.darwinsys.security.DigestUtils;
import data.MemberDAO;
import jakarta.transaction.Transactional;
import model.Member;
import model.MemberRole;
import webtier.LoginConstants;
import webtier.WebConstants;

/**
 * Simple DIY login. Can't use vanilla CMS because we need to
 * get the login info from our database. Could we use a Realm?
 * Maybe, but the code in login() here wouldn't be much different.
 */
@Named("identity")
@SessionScoped
public class LoginAction implements Serializable {

	private static final long serialVersionUID = -1899830327382597511L;

	@Inject MemberDAO memberDAO;
	private HttpSession httpSession;
	private String email, password;
	private Member loggedInUser; // public only for use in tests

	public void setMemberDAO(MemberDAO memberDAO) {
		this.memberDAO = memberDAO;
	}

	// XXX Extract JSF deps outta here, have a pluggable message(String) method.
	Consumer<String> jsfTalker = s ->
		FacesContext.getCurrentInstance().addMessage( null, new FacesMessage(s));

	Consumer<String> printer = jsfTalker;
	public void setPrinter(Consumer<String> printer) {
		this.printer = printer;
	}
	
	public LoginAction() {
		// Just for debugging...
		System.out.println("LoginAction.LoginAction()");
	}

	/** =================== The main login method =================== */
	@Transactional
	public String login() {
		System.out.println("Login.login(" + email + ")");
		try {
			final Optional<Member> memberOptional =
					memberDAO.findByEmailAndPassword(email, DigestUtils.md5(password));
			if (memberOptional.isEmpty()) {
				printer.accept("Failed to log in as " + email);
				return "Login";

			}
			this.loggedInUser = memberOptional.get();
			HttpSession session = getHttpSession();
			session.setAttribute(LoginConstants.LOGIN_MEMBER, loggedInUser);
			session.setAttribute(LoginConstants.LOGIN_FLAG, this);

			// XXX Maybe now tell the Container about it for e.g., logging and session times...
			// using request.login(email, password);
			// Except that EE wants a login and password to look up in a Realm, which we've already done.

			printer.accept("Logged in as " + getLoggedInUser().getName());

			// Give logged-in user a bit of time
			final Duration timeout = Duration.parse("PT2H");;
			session.setMaxInactiveInterval((int) timeout.getSeconds());

			// See if they got here by redirect from protected page
			String redirTarget = (String) session
					.getAttribute(LoginConstants.TARGET_URI_KEY);
			if (redirTarget != null) {
				System.out.println("Return stored " + redirTarget);
				session.removeAttribute(LoginConstants.TARGET_URI_KEY);
				return redirTarget + WebConstants.FORCE_REDIRECT;
			}
			return "/index.web" + WebConstants.FORCE_REDIRECT;

		} catch (NoResultException nre) {
			System.out.println("LoginAction.login():" + nre);
			printer.accept("Login Failed for user " + email + "; user and/or password invalid.");
            return "Login";
		} catch (RuntimeException ex) {
			System.out.println("LoginAction.login():" + ex);
			Throwable exc, r = ((exc = ex.getCause()) == null ? ex : exc);
			r.printStackTrace();
			printer.accept("Login Failed for user " + email + " due to " + ex);
			return "Login";
		}
	}

	/** Does what it says on the tin. */
	public String logout() {
		System.out.println("Logout " + getLoggedInUser());
		getHttpSession().invalidate();
		printer.accept("You are being logged out  as " + getLoggedInUser().getName());
		this.loggedInUser = null;
		return LoginConstants.HOME_PAGE + WebConstants.FORCE_REDIRECT;
	}

	/** Called by JSF */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/** Used by JSF */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/** Important for security */
	public boolean isLoggedIn() {
		return getLoggedInUser() != null;
	}

	public Member getLoggedInUser() {
		return loggedInUser;
	}

	/** Settable from tests, has to be method since can't have public fields */
	public void setLoggedInUser(Member m) {
		this.loggedInUser = m;
	}

	/** Settable from test, to avoid JSF dependency */
	public HttpSession getHttpSession() {
		if (httpSession == null) {
			httpSession =
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		}
		return httpSession;
	}

	/** Used in testing, do not inline! */
	public void setHttpSession(HttpSession session) {
		this.httpSession = session;
	}

	/** Important for security */
	public boolean hasRole(String role) {
		if (role == null) {
			throw new NullPointerException("isUserInRole(null) isn't sensible");
		}
		if (!isLoggedIn()) {
			return false;
		}
		for (MemberRole r : getLoggedInUser().getRoles()) {
			if (r.getName().equals(role)) {
				return true;
			}
		}
		// "Synthetic" exec "role" based on "position" property of member
		// e.g., anything in member.position allows the "exec" role.
		// Needs to be here for JSF hasRole() in xxx.pages.xml; can't hack
		// it into roles in login() because it might get persisted, then
		// you'd have a maintenance problem (it would persist after you
		// removed their position when they relinquish it).
		if ("exec".equals(role)) {
			return getLoggedInUser().getPosition()!=null &&
                    !getLoggedInUser().getPosition().trim().isEmpty();
		}
		return false;
	}
}
