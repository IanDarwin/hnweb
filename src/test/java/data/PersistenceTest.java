package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import model.Member;

public class PersistenceTest extends AbstractPersistenceTest {

	final static String FIRST = "Loodlie";

	/** Live JPA (fake database): save a Member, then
	 * assert that we find it in the list of Members from JPA
	 */
	@Test
	public void testLoadStore() {
		Member tm = new Member();
		tm.setFirstName(FIRST);
		tm.setLastName("Lozz");
		tm.setEmailTrips(true);
		tm.addRole("Dumpster Driver");

		em.getTransaction().begin();
		em.persist(tm);
		em.getTransaction().commit();

		List<Member> ms = em.createQuery("from Member", Member.class).getResultList();
		assertTrue("found any", ms.size() > 0);

		for (Member m : ms) {
			if (m.getFirstName().equals(FIRST) && m.isEmailTrips()) {
				assertEquals(1, m.getRoles().size());
				assertTrue(m.getRoles().contains("Dumpster Driver"));
				return;
			}
		}
		fail("Did not retrieve Member that we saved!");
	}
}
