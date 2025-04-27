package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import model.MemberRole;
import org.junit.jupiter.api.Test;

import model.Member;

public class PersistenceTest extends AbstractPersistenceTest {

	final static String FIRST = "Loodlie";
	public static final MemberRole ROLE = new MemberRole("Dumpster Driver");

	/** Live JPA (fake database): save a Member, then
	 * assert that we find it in the list of Members from JPA
	 */
	@Test
	public void testLoadStore() {
		Member tm = new Member();
		tm.setFirstName(FIRST);
		tm.setLastName("Lolz");
		tm.setEmailTrips(true);
		tm.addRole(ROLE);

		em.getTransaction().begin();
		em.persist(ROLE);
		em.persist(tm);
		em.getTransaction().commit();

		List<Member> ms = em.createQuery("select m from Member m", Member.class).getResultList();
		assertTrue("found any", ms.size() > 0);

		for (Member m : ms) {
			if (m.getFirstName().equals(FIRST) && m.isEmailTrips()) {
				assertEquals(1, m.getRoles().size());
				assertTrue(m.getRoles().get(0).getName().equals("Dumpster Driver"));
				return;
			}
		}
		fail("Did not retrieve Member that we saved!");
	}
}
