package data;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import model.Member;

import java.io.Serializable;
import java.util.List;

@Named("memberListView")
@ViewScoped
public class MemberListView implements Serializable {
	@Inject MemberDAO memberDAO;
	private List<Member> members;
	private boolean loaded;

	public void load() {
		members = memberDAO.findAll();   // the slow part, now in the AJAX request
		loaded = true;
	}
	public boolean isLoaded() { return loaded; }
	public List<Member> getMembers() { return members; }
}