package action;

import java.util.*;
import javax.faces.*;
import javax.inject.*;

import model.*;

@ViewScoped
public class MemberCompleteAction {

	public MemberCompleteAction() {
		System.out.println("MemberCompleteAction()");
	}

	@Inject
	MemberList service;

	public List<Member> completeMember(String query) {
        List<Member> allMembers = service.getMembers();
        List<Member> filteredMembers = new ArrayList<Member>();
         
        for (Member m : allMembers) {
            if(m.getLastName().toLowerCase().contains(query)) {
                filteredMembers.add(m);
            }
        }
         
        return filteredMembers;
    }
}
