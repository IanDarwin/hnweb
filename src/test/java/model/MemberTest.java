package model;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class MemberTest {

	Member member;
	
	@Before
	public void test() {
		member = new Member();
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
		member.setExpiryYear(LocalDate.now().getYear());
		assertFalse(member.isExpired());
	}
	@Test
	public void testIsExpiredTrue() {
		member.setExpiryYear(LocalDate.now().getYear() - 1);
		assertTrue(member.isExpired());
	}
}
