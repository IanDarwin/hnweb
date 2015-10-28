package infrastructure;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import model.Member;
import data.MemberList;


/**
 * Simple DIY login.
 */
@ManagedBean(name="login") @SessionScoped
public class Login implements Serializable {

	private static final long serialVersionUID = -1899830327382597511L;

	@Inject
	MemberList userlist;

	String username, password;

	Member loggedInUser = null;
	
	public Login() {
		// Just for debugging...
		System.out.println("Login.Login()");
	}
	
	public String login() {
		System.out.println("Login.login(" + username + ")");
		try {
			loggedInUser = 
				userlist.findByUserNameAndPassword(username, hash(password));
			FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage("Logged in as " + loggedInUser.getName()));
			return "MemberList";
		} catch (RuntimeException ex) {
			Throwable exc, r = ((exc = ex.getCause()) == null ? ex : exc);
			r.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage("Login Failed for user " + username + " due to " + ex));
			return "Login";
		}
	}

	/** Called by JSF */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/** Used by JSF */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/** Just to compile */
	private String hash(String input) {
		return input;
	}
}
		
