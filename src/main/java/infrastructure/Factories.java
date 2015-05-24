package infrastructure;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Named;

import model.MemberType;

@Singleton
@Named
public class Factories {

	public List<MemberType> getMemberTypes() {
		return Arrays.asList(MemberType.values());
	}

}
