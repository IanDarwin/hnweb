package model;



import java.time.LocalDate;

import action.YearCalcs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MemberTest {

	Member member;

	@BeforeEach
	void test() {
		member = new Member();
	}

	@Test
	void copyConstructor() {
		MemberArchived memberArchived = new MemberArchived();
		memberArchived.setFirstName("Thelmius");
		memberArchived.setLastName("Aquaddapodox");
		var member = new Member(memberArchived);
		assertEquals("Thelmius", member.getFirstName());
		assertEquals("Aquaddapodox", member.getLastName());
	}

	@Test
	void getName2() {
		member.setFirstName("Thelmius");
		member.setLastName("Aquaddapodox");
		assertEquals("Thelmius Aquaddapodox", member.getName());
	}

	@Test
	void getName3() {
		member.setFirstName("Thelmius");
		member.setMiddle("T.");
		member.setLastName("Aquaddapodox");
		assertEquals("Thelmius T. Aquaddapodox", member.getName());
	}

	@Test
	void isExpiredFalse() {
		final int curYear = LocalDate.now().getYear();
		member.setExpiryYear(curYear);
		var d = LocalDate.of(curYear, Config.RENEWAL_MONTH - 1, 1);
		assertFalse(YearCalcs.isExpired(member, d));
		var e = LocalDate.of(curYear, Config.RENEWAL_MONTH, 1);
		assertFalse(YearCalcs.isExpired(member, e));
		var f = LocalDate.of(curYear, Config.RENEWAL_MONTH + 1, 1);
		assertTrue(YearCalcs.isExpired(member, f));
	}

	@Test
	void isExpiredTrue() {
		member.setExpiryYear(LocalDate.now().getYear() - 1);
		assertTrue(YearCalcs.isExpired(member));
	}
}
