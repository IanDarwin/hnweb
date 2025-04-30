package action;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import model.Member;
import model.MemberArchived;
import model.MemberTransaction;

/**
 * Remove or delete, or undelete, a Member
 */
@Stateful @Named @RequestScoped
public class RemoveAction {
	
	@PersistenceContext public EntityManager em;

	public long id;	// only used in undelete case

	@Transactional
	public String remove(Member instance) {

		// Use raw sql to archive, via JPA Native Query mechanism
		// String archive_sql = "delete from transaction where member_id = ?1";
		// em.createNativeQuery(archive_sql).
			// setParameter(1, instance.getId()).
			// executeUpdate();

		// Remove secondaries, if any, before primary
		for (Member sm : instance.getSecondaryMembers()) {
			em.remove(em.merge(sm));
		}

		// Remove transactions for this user
		for (MemberTransaction tx : instance.getMemberTransactions()) {
			em.remove(em.merge(tx));
		}

		// Finally remove the member instance
		em.remove(em.merge(instance));
		em.persist(new MemberArchived(instance));
		em.flush();
		System.out.printf("Moved %s to archive!\n", instance);
		return "MemberList";
	}

	/**
	 * Restore, undelete, unarchive, necromance: whatever, it partly reverses the above.
	 * Transactions, Secondaries, etc. all get lost in the shuffle.
	 */
	@Transactional
	public String unDelete(long id) {
		// The dead records don't (shouldn't) have MemberTransactions, so we can just restore
		MemberArchived instance = em.find(MemberArchived.class, id);
		if (instance == null) {
			throw new IllegalArgumentException("MemberArchived " + id + " not found.");
		}
		System.out.println("Trying to necromance " + instance + " with id " + id);
		em.remove(instance);
		em.flush();
		em.persist(new Member(instance));
		em.flush();	// Ensure prints with updated id
		System.out.printf("Restored %s from archive!\n", instance);
		return "MemberList";
	}

	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
}

