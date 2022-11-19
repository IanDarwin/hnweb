package model;



import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MemberTest {

	Member member;
	
	@BeforeEach
	public void test() {
		member = new Member();
	}

	@Test
	public void testCopyConstructor() {
		MemberArchived memberArchived = new MemberArchived();
		memberArchived.setFirstName("Thelmius");
		memberArchived.setLastName("Aquaddapodox");
		var member = new Member(memberArchived);
		assertEquals("Thelmius", member.getFirstName());
		assertEquals("Aquaddapodox", member.getLastName());
	}
	
	@Test
	public void testGetName2() {
		member.setFirstName("Thelmius");
		member.setLastName("Aquaddapodox");
		assertEquals("Thelmius Aquaddapodox", member.getName());
	}
	
	@Test
	public void testGetName3() {
		member.setFirstName("Thelmius");
		member.setMiddle("T.");
		member.setLastName("Aquaddapodox");
		assertEquals("Thelmius T. Aquaddapodox", member.getName());
	}

	@Test
	public void testIsExpiredFalse() {
		final int curYear = LocalDate.now().getYear();
		member.setExpiryYear(curYear);
		member.setNow(LocalDate.of(curYear, Config.RENEWAL_MONTH - 1, 1));
		assertFalse(member.isExpired());
		member.setNow(LocalDate.of(curYear, Config.RENEWAL_MONTH, 1));
		assertFalse(member.isExpired());
		member.setNow(LocalDate.of(curYear, Config.RENEWAL_MONTH + 1, 1));
		assertTrue(member.isExpired());
	}

	@Test
	public void testIsExpiredTrue() {
		member.setExpiryYear(LocalDate.now().getYear() - 1);
		assertTrue(member.isExpired());
	}
}
