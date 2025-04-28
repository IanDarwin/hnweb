package data;

import static org.junit.Assert.*;

import action.RemoveAction;
import model.Member;
import org.junit.jupiter.api.Test;

public class RemoveActionTest extends AbstractPersistenceTest {

	@Test
	public void testRemoveAction() {
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
