package action;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import com.darwinsys.security.DigestUtils;

import data.MemberList;
import model.Member;
import webtier.LoginConstants;


/**
 * Simple DIY login.
 */
@ManagedBean(name="identity") @SessionScoped
public class LoginAction implements Serializable {

	private static final long serialVersionUID = -1899830327382597511L;

	@Inject MemberList userlist;

	String email, password;

	private Member loggedInUser = null;
	
	public LoginAction() {
		// Just for debugging...
		System.out.println("LoginAction.LoginAction()");
	}
	
	public String login() {
		System.out.println("Login.login(" + email + ")");
		try {
			setLoggedInUser(verifyUser(email, password));
			if (getLoggedInUser() == null) {
				throw new IllegalStateException("findByEmailAndPassword should not return null!");
			}

			// XXX Maybe now tell the Container about it for e.g., logging and session times...
			// using request.login(email, password);
			// Except that they want a login and password to look up in a Realm, which we've already done.

			FacesContext.getCurrentInstance().addMessage( null,
                    new FacesMessage("Logged in as " + getLoggedInUser().getName()));
			return "index.web";

		} catch (NoResultException nre) {
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Login Failed for user " + email + "; user and/or password invalid."));
            return "Login";
		} catch (RuntimeException ex) {
			Throwable exc, r = ((exc = ex.getCause()) == null ? ex : exc);
			r.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Login Failed for user " + email + " due to " + ex));
			return "Login";
		}
	}

	Member verifyUser(String email, String password) {
		return userlist.findByEmailAndPassword(email, DigestUtils.md5(password));
	}

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
		return false;
	}

	public void setLoggedInUser(Member loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
}
