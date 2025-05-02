package data;

import static org.junit.Assert.*;

import action.RemoveAction;
import model.Member;
import org.junit.jupiter.api.Test;

public class RemoveActionTest extends AbstractPersistenceTest {

	@Test
	public void testRemoveAction() {
		entityManager.getTransaction().begin();
		Member member = new Member("A", "B", "C", "d@e.fg");
		entityManager.persist(member);
		long id = member.getId();
		entityManager.getTransaction().commit();

		Member m2 = entityManager.find(Member.class, id);
		final RemoveAction removeAction = new RemoveAction();
		removeAction.memberDAO = new MemberDAO(entityManager);
		entityManager.getTransaction().begin();
		removeAction.remove(m2);
		entityManager.getTransaction().commit();
		assertNull(entityManager.find(Member.class, id));
	}
}
