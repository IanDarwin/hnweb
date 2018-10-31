package data;

import org.junit.Test;
import static org.junit.Assert.*;

import action.RemoveAction;
import model.Member;

public class RemoveActionTest extends AbstractPersistenceTest {

	@Test
	//@Ignore // "insert ... select" Works in a real database, fails in H2
	public void testRemove() {
		MemberHome home = new MemberHome();
		home.em = em;
		em.getTransaction().begin();
		Member member = new Member("A", "B", "C", "d@e.fg");
		em.persist(member);
		em.flush();
		long id = member.getId();
		final RemoveAction removeAction = new RemoveAction();
		removeAction.em = em;
		removeAction.remove(member);
		em.getTransaction().commit();
		System.out.println("Remove did not throw exception");
		assertNull(em.find(Member.class, id));
	}
}
