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

@Stateful @Named @RequestScoped
public class RemoveAction {
	
	@PersistenceContext
	public EntityManager em;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String remove(Member instance) {

		// Use raw sql to archive, via JPA Native Query mechanism
		String archive_sql = "delete from transaction where member_id = ?1";
		int rowCount = 
			em.createNativeQuery(archive_sql).
			setParameter(1, instance.getId()).
			executeUpdate();
		// Do not need to check rowcount.
		em.persist(new MemberArchived(instance));
		em.remove(instance);
		System.out.println("Removed!");
		return "MemberList";
	}
}
