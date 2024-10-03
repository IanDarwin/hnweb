package data;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import action.RemoveAction;
import model.Member;

public class RemoveActionTest extends AbstractPersistenceTest {

	@Test
	@Ignore // "insert ... select" Works in a real database, fails in H2
	public void testRemove() {
		MemberHome home = new MemberHome();
		home.em = em;
		em.getTransaction().begin();
		Member member = new Member("A", "B", "C", "d@e.fg");
		em.persist(member);
		long id = member.getId();
		em.getTransaction().commit();

		Member m2 = em.find(Member.class, id);
		final RemoveAction removeAction = new RemoveAction();
		removeAction.em = em;
		em.getTransaction().begin();
		removeAction.remove(m2);
		em.getTransaction().commit();
		assertNull(em.find(Member.class, id));
	}
}
