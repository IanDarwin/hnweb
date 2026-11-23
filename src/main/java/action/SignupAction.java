package action;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Config;
import model.MemberType;
import model.UnconfirmedMember;
import outreach.MailMan;
import webtier.WebConstants;

/**
 * Backs Signup1.xhtml (the form) and Signup2.xhtml (the page the emailed link opens).
 * <ol>
 * <li>{@link #signup1()}: save the signup as an unconfirmed member and email a link.
 * <li>{@link #confirm()}: run when the link is opened; confirms the signup.
 * </ol>
 * The database work is in {@link SignupService}.
 * <p>
 * Settings (config.properties): <b>site.baseurl</b> (optional; the public address of this app, used in the
 * emailed link, e.g. https://hn.darwinsys.com; if absent it is taken from the request, which is wrong
 * behind some proxies), <b>signup.notify</b> (optional; an address told about each confirmed signup).
 */
@Named
@RequestScoped
public class SignupAction implements Serializable {

	private static final long serialVersionUID = 332428792829209817L;

	private static final Logger logger = Logger.getLogger(SignupAction.class.getName());

	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	// Form fields
	private MemberType memberType = MemberType.Individual;
	private String firstName;
	private String lastName;
	private String email;
	private String city;
	private String homePhone;
	private String cellPhone;
	private String workPhone;
	private String donation;
	private String interests;
	private String suggestions;
	private boolean consent;
	/** A honeypot: invisible to people, so only robots fill it in */
	private String website;

	// From the emailed link
	private String token;

	@Inject
	SignupService service;
	@Inject
	MailMan mailMan;

	/**
	 * Step 1: save the signup, unconfirmed, and email the confirmation link.
	 * @return Where to go next; null (stay here) on error.
	 */
	public String signup1() {
		if (website != null && !website.isBlank()) {
			logger.info("Signup honeypot tripped; pretending all is well");
			return sentPage();
		}
		if (email == null || !EMAIL.matcher(email.trim()).matches()) {
			return fail("Please enter a valid email address.");
		}
		if (!consent) {
			return fail("Please tick the box to let the club contact you; we can't sign you up without it.");
		}
		if (memberType == MemberType.Secondary) {
			return fail("You can't add secondary members here; please contact the membership admin.");
		}
		if (memberType == null) {
			return fail("Please choose a membership type.");
		}

		var u = new UnconfirmedMember();
		u.setFirstName(firstName.trim());
		u.setLastName(lastName.trim());
		u.setEmail(email.trim());
		u.setCity(blankToNull(city));
		u.setHomePhone(blankToNull(homePhone));
		u.setCellPhone(blankToNull(cellPhone));
		u.setWorkPhone(blankToNull(workPhone));
		u.setMemberType(memberType);
		u.setNotes(notes());

		SignupService.Begun begun;
		try {
			begun = service.begin(u);
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Signup failed for " + email, e);
			return fail("Sorry, we couldn't process your signup. Please try again, or use the paper form.");
		}

		try {
			if (begun.status() == SignupService.BeginStatus.TOKEN_CREATED) {
				String link = baseUrl() + "/Signup2.web?token=" + begun.token();
				mailMan.sendMessageTo(u.getEmail(), config("orgName") + " signup: please confirm your email",
					"Hello " + u.getFirstName() + ",<br/><br/>"
					+ "Thank you for signing up with " + config("orgName") + "! "
					+ "Please confirm your email address by clicking this link:<br/><br/>"
					+ "<a href=\"" + link + "\">" + link + "</a><br/><br/>"
					+ "The link works for " + SignupService.TOKEN_HOURS + " hours. "
					+ "If you didn't sign up, just ignore this email and nothing will happen.");
			} else {
				// Say the same thing on screen either way, so the form can't be used to find out
				// who is a member; the person who owns this address gets the useful answer.
				mailMan.sendMessageTo(u.getEmail(), config("orgName") + " signup",
					"Hello,<br/><br/>Someone (we hope you) just tried to sign up with this email address, "
					+ "but it is already in our membership records. "
					+ "To log in, or to set or reset your password, please visit<br/><br/>"
					+ "<a href=\"" + baseUrl() + "/Login.web\">" + baseUrl() + "/Login.web</a>");
			}
		} catch (Exception e) {
			// The signup stays saved; trying again simply replaces it
			logger.log(Level.WARNING, "Could not email signup link to " + u.getEmail(), e);
			return fail("We couldn't send your confirmation email. Please check the address and try again.");
		}
		logger.info("Signup " + begun.status() + " for " + u);
		return sentPage();
	}

	/**
	 * Step 2: the person clicked the link in the email. Run by Signup2.xhtml's viewAction.
	 * @return The thank-you page on success; null (stay here and explain) otherwise.
	 */
	public String confirm() {
		SignupService.Confirmation c;
		try {
			c = service.confirm(token);
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Signup confirmation failed", e);
			return fail("Sorry, something went wrong confirming your signup. Please try again later.");
		}
		switch (c.status()) {
		case CONFIRMED:
			logger.info("Signup confirmed: " + c.name() + " <" + c.email() + "> " + c.type());
			notifyClub(c);
			return "/SignupDone" + WebConstants.FORCE_REDIRECT + "&step=confirmed";
		case EXPIRED:
			return fail("This confirmation link has expired. Please sign up again.");
		default:
			return fail("This confirmation link isn't valid. If you've already used it, "
				+ "you're all set. Otherwise, please sign up again.");
		}
	}

	private void notifyClub(SignupService.Confirmation c) {
		String to = Config.getProperty("signup.notify");
		if (to == null || to.isBlank()) {
			return;
		}
		try {
			mailMan.sendMessageTo(to.trim(), "New member signup: " + c.name(),
				c.name() + " &lt;" + c.email() + "&gt; confirmed a signup as " + c.type()
				+ ". They are not marked as paid until you record their payment.");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not send signup notice", e);	// the signup itself is done
		}
	}

	// ---------- helpers ----------

	private String sentPage() {
		return "/SignupDone" + WebConstants.FORCE_REDIRECT + "&step=sent";
	}

	/** Free-text answers from the form have no column of their own; keep them in the notes */
	private String notes() {
		var sb = new StringBuilder("Signed up online.");
		if (donation != null && !donation.isBlank()) {
			sb.append(" Donation intended: $").append(donation.trim()).append('.');
		}
		if (interests != null && !interests.isBlank()) {
			sb.append(" Interests: ").append(interests.trim().replaceAll("\\s+", " ")).append('.');
		}
		if (suggestions != null && !suggestions.isBlank()) {
			sb.append(" Suggestions: ").append(suggestions.trim().replaceAll("\\s+", " ")).append('.');
		}
		return sb.length() > 250 ? sb.substring(0, 250) : sb.toString();	// the notes column is varchar(255)
	}

	private static String blankToNull(String s) {
		return (s == null || s.isBlank()) ? null : s.trim();
	}

	private static String config(String key) {
		String v = Config.getProperty(key);
		return v == null ? "the club" : v;
	}

	/** The public address of this app, no trailing slash, for links in email */
	private static String baseUrl() {
		String configured = Config.getProperty("site.baseurl");
		if (configured != null && !configured.isBlank()) {
			String c = configured.trim();
			return c.endsWith("/") ? c.substring(0, c.length() - 1) : c;
		}
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		String scheme = ec.getRequestScheme();
		int port = ec.getRequestServerPort();
		boolean dflt = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
		return scheme + "://" + ec.getRequestServerName() + (dflt ? "" : ":" + port) + ec.getRequestContextPath();
	}

	private String fail(String message) {
		FacesContext.getCurrentInstance().addMessage(null,
			new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
		return null;	// Stay on page
	}

	// ---------- properties ----------

	public MemberType getMemberType() { return memberType; }
	public void setMemberType(MemberType memberType) { this.memberType = memberType; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }

	public String getHomePhone() { return homePhone; }
	public void setHomePhone(String homePhone) { this.homePhone = homePhone; }

	public String getCellPhone() { return cellPhone; }
	public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }

	public String getWorkPhone() { return workPhone; }
	public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }

	public String getDonation() { return donation; }
	public void setDonation(String donation) { this.donation = donation; }

	public String getInterests() { return interests; }
	public void setInterests(String interests) { this.interests = interests; }

	public String getSuggestions() { return suggestions; }
	public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

	public boolean isConsent() { return consent; }
	public void setConsent(boolean consent) { this.consent = consent; }

	public String getWebsite() { return website; }
	public void setWebsite(String website) { this.website = website; }

	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
}
