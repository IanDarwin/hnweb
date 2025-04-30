package data;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;
import jakarta.inject.Inject;

import model.Member;

/**
 * Class to provide list of members matching a string query,
 * for use in web tier, e.g., administrative Renew Member function.
 */
@Named
@RequestScoped
public class MemberCompleteAction {

	@Inject@Default
	MemberDAO service;
	
	List<Member> allMembers;

	/** non-public; for testing */
	void setAllMembers(List<Member> allMembers) {
		this.allMembers = allMembers;
	}

	public List<Member> completeMember(String query) {
		if (allMembers == null) {
			allMembers = service.findNonSecondaries();
		}
        List<Member> filteredMembers = new ArrayList<Member>();
        for (Member m : allMembers) {
        	if (m.getLastName().toLowerCase().contains(query.toLowerCase())) {
        		filteredMembers.add(m);
        	}
        }
         
        return filteredMembers;
	}
}
