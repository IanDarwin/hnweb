package action;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Member;
import model.MemberArchived;
import model.MemberBase;

@Stateful @Named @RequestScoped
public class RemoveAction {
	
	@PersistenceContext
	public EntityManager em;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String remove(Member instance) {

		// Use raw sql to archive, via JPA Native Query mechanism
		// String archive_sql = "delete from transaction where member_id = ?1";
		// em.createNativeQuery(archive_sql).
			// setParameter(1, instance.getId()).
			// executeUpdate();

		// Remove secondaries, if any, before primary
		for (MemberBase sm : instance.getSecondaryMembers()) {
			em.remove(em.merge(sm));
		}

		// Do we also have to remove any roles?
		// javax.persistence.PersistenceException: org.hibernate.HibernateException: 
		// Illegal attempt to associate a collection with two open sessions. Collection : [model.Member.roles#233]

		// Finally remove the member instance
		em.remove(em.merge(instance));
		em.flush();
		// This is all one transaction despite the flush (hopes).
		em.persist(new MemberArchived(instance));
		System.out.println("Removed!");
		return "MemberList";
	}
}
