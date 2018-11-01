package webtier;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import data.MemberList;

@Named("memberConverter")
@ApplicationScoped
public class MemberJsfConverter implements Converter {

	@Inject()
	MemberList memberList;
	
	public MemberJsfConverter() {
		System.out.println("MemberJsfConverter.MemberJsfConverter()");
	}
	
	public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
		System.out.printf("MemberJsfConverter.getAsObject(,,%s)%n", value);
		if (value == null || value.trim().length() == 0) {
			return null;
		} else {
			try {
				long id = Long.parseLong(value);
				return memberList.findBy(id);
			} catch (NumberFormatException e) {
				throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid Member."));
			}
		}
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		System.out.printf("MemberJsfConverter.getAsString(,,%s)%n", object);
		if (object == null) {
			return null;
		} else {
			if (Long.class != object.getClass()) {
				throw new IllegalArgumentException("getAsString expects long arg");
			}
			return memberList.findBy((Long)object).toString();
		}
	}   
}     

