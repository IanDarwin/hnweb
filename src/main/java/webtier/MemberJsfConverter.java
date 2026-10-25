package webtier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import data.MemberDAO;
import model.Member;

/**
 * Converts between a {@link Member} and its primary key, for the
 * p:autoComplete in Renew.xhtml, which must use
 * <code>itemValue="#{_member}"</code> (the object itself) with
 * <code>itemLabel="#{_member.names}"</code> for display.
 * <p>
 * The primary key is used on the wire (not the displayed name) because names
 * are neither unique nor free of commas, so parsing "Last, First" back into a
 * Member is unreliable.
 *
 * @author Ian Darwin
 */
@Named("memberConverter")
@ApplicationScoped
public class MemberJsfConverter implements Converter<Member> {

	@Inject
	MemberDAO memberDAO;

	/** Input is the primary key, as produced by {@link #getAsString}. */
	@Override
	public Member getAsObject(FacesContext fc, UIComponent uic, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final Member member;
		try {
			member = memberDAO.findById(Long.valueOf(value.trim()));
		} catch (NumberFormatException e) {
			throw notAMember();
		}
		if (member == null) {
			throw notAMember();
		}
		return member;
	}

	/** Output is the primary key. */
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Member member) {
		if (member == null || member.getId() == null) {
			return "";
		}
		return member.getId().toString();
	}

	private static ConverterException notAMember() {
		return new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Member not found", "Pick a member from the list that appears as you type."));
	}
}
