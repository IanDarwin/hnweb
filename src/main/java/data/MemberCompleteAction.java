package data;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

import model.Member;

@Named
@SessionScoped
public class MemberCompleteAction {

	@Inject
	MemberList service;
	
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
