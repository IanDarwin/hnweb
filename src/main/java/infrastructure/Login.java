package infrastructure;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import data.MemberList;


/**
 * Simple DIY login.
 */
@Named("login") @ApplicationScoped
public class Login {

	@Inject
	MemberList userlist;
	
	String username, password;

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

	public String login() {
		try {
			userlist.findByUsernameAndPassword(username, hash(password));
			return "PersonList";
		} catch (RuntimeException ex) {
			// XXX create a FacesMessage with the failure
			return "Login";
		}
	}

	/** Just to compile */
	private String hash(String input) {
		return input;
	}
}
		
