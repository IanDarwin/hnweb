package data;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.*;

import model.Member;

public class MemberHomeTest extends AbstractPersistenceTest {

	@Test
	public void testGetNewMemberExpiryYear() {
		LocalDate aug31 = LocalDate.of(2011, 8, 29);
		assertEquals(2011, MemberHome.getNewMemberExpiryYear(aug31));
		LocalDate sep1 = LocalDate.of(2011, 9, 01);
		assertEquals(2012, MemberHome.getNewMemberExpiryYear(sep1));
	}
	
	@Test
	@Ignore // "insert ... select" Works in a real database, fails in H2
	public void testRemove() {
		MemberHome home = new MemberHome();
		home.em = em;
		em.getTransaction().begin();
		Member member = new Member("A", "B", "C", "d@e.fg");
		em.persist(member);
		home.instance = member;
		home.remove();
		System.out.println("Remove did not throw exception");
	}

}
