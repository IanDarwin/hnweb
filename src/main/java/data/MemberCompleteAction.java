package data;

import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import model.Member;

@Named
@ViewScoped
public class MemberCompleteAction {

	public MemberCompleteAction() {
		System.out.println("MemberCompleteAction()");
	}

	@Inject
	MemberList service;
	
	List<Member> allMembers;

	void setAllMembers(List<Member> allMembers) {
		this.allMembers = allMembers;
	}

	public List<Member> completeMember(String query) {
		if (allMembers == null) {
			allMembers = service.findAll();
		}
        List<Member> filteredMembers = new ArrayList<Member>();
        for (Member m : allMembers) {
        	if (m.getLastName().toLowerCase().contains(query)) {
        		filteredMembers.add(m);
        	}
        }
         
        return filteredMembers;
	}
}
