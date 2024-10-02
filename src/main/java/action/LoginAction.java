package action;

import java.io.Serializable;
import java.util.Optional;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpSession;

import com.darwinsys.security.DigestUtils;
import data.MemberList;
import model.Member;
import webtier.LoginConstants;
import webtier.PageFilter;
import webtier.WebConstants;

/**
 * Simple DIY login.
 */
@Named("identity")
@SessionScoped
public class LoginAction implements Serializable {

	private static final long serialVersionUID = -1899830327382597511L;

	@Inject MemberList memberList;

	public void setMemberList(MemberList memberList) {
		this.memberList = memberList;
	}

	String email, password;

	private Member loggedInUser = null;
	
	public LoginAction() {
		// Just for debugging...
		System.out.printf("LoginAction.LoginAction(): %d users\n",
				-1 /*userlist.findAll().count()*/);
	}
	
	public String login() {
		System.out.println("Login.login(" + email + ")");
		try {
			final Optional<Member> memberOptional = verifyUser(email, password);
			if (memberOptional.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage( null,
						new FacesMessage("Failed to log in as " + email));

			}
			setLoggedInUser(memberOptional.get());
			setUserInSession(loggedInUser);

			// XXX Maybe now tell the Container about it for e.g., logging and session times...
			// using request.login(email, password);
			// Except that EE wants a login and password to look up in a Realm, which we've already done.

			FacesContext.getCurrentInstance().addMessage( null,
                    new FacesMessage("Logged in as " + getLoggedInUser().getName()));
			final HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
					.getExternalContext()
					.getSession(true);

			// Give logged-in user a bit of time
			final int timeout = 2 * 60 * 60;
			session.setMaxInactiveInterval(timeout);

			// See if they got here by redirect from protected page
			String redirTarget = (String) session
					.getAttribute(LoginConstants.TARGET_URI_KEY);
			if (redirTarget != null) {
				System.out.println("Return stored " + redirTarget);
				session.removeAttribute(LoginConstants.TARGET_URI_KEY);
				return redirTarget + WebConstants.FORCE_REDIRECT;
			}
			return "/index.web";

		} catch (NoResultException nre) {
			System.out.println("LoginAction.login():" + nre);
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Login Failed for user " + email + "; user and/or password invalid."));
            return "Login";
		} catch (RuntimeException ex) {
			System.out.println("LoginAction.login():" + ex);
			Throwable exc, r = ((exc = ex.getCause()) == null ? ex : exc);
			r.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Login Failed for user " + email + " due to " + ex));
			return "Login";
		}
	}

	/** Hook allows testing via mocking; do not inline! */
	Optional<Member> verifyUser(String email, String password) {
		return Optional.ofNullable(memberList.findByEmailAndPassword(email, DigestUtils.md5(password)));
	}

	/** Does what it says on the tin. */
	public String logout() {
		System.out.println("Logout " + getLoggedInUser());
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		FacesContext.getCurrentInstance().addMessage( null,
                new FacesMessage("You are being logged out  as " + getLoggedInUser().getName()));
		setLoggedInUser(null);
		return LoginConstants.HOME_PAGE + "?faces-redirect=true";
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

	/** Important for security */
	public boolean hasRole(String role) {
		if (role == null) {
			throw new NullPointerException("isUserInRole(null) isn't sensible");
		}
		if (!isLoggedIn()) {
			return false;
		}
		for (String r : getLoggedInUser().getRoles()) {
			if (r.equals(role)) {
				return true;
			}
		}
		// "Synthetic" exec "role" based on "position" property of member
		// Needs to be here for JSF hasRole() in xxx.pages.xml; can't hack
		// it into roles in login() because it might get persisted, then
		// you'd have a maintenance problem (it would persist after you
		// removed their position when they relinquish it).
		if ("exec".equals(role)) {
			return getLoggedInUser().getPosition()!=null &&
				getLoggedInUser().getPosition().trim().length() > 0;
		}
		return false;
	}

	public void setLoggedInUser(Member loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public void setUserInSession(Member loggedInUser) {
		HttpSession session =
                (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		session.setAttribute(LoginConstants.LOGIN_MEMBER, loggedInUser);
		session.setAttribute(LoginConstants.LOGIN_FLAG, this);
	}
}
