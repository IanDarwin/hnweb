package action;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import data.MemberDAO;
import model.Member;
import model.MemberArchived;
import model.MemberTransaction;

/**
 * Remove or delete, or undelete, a Member
 */
@Stateful @Named @RequestScoped
public class RemoveAction {
	
	@Inject public MemberDAO memberDAO;

	public long id;	// only used in undelete case

	@Transactional
	public String remove(Member instance) {
		memberDAO.delete(instance);
		System.out.printf("Delete, and moved %s to archive!\n", instance);
		return "MemberList";
	}

	/**
	 * Restore, undelete, unarchive, necromance: whatever, it partly reverses the above.
	 * Transactions, Secondaries, etc. all get lost in the shuffle.
	 */
	@Transactional
	public String unDelete(long id) {
		memberDAO.undelete(id);
		return "MemberDAO";
	}

	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
}

