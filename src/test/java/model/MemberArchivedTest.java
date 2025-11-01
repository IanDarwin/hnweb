package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberArchivedTest {

	Member member;

	@Test
	void copyConstructor() {
		member = new Member();
		member.setFirstName("Thelmius");
		member.setLastName("Aquaddapodox");
		var memberArchived = new MemberArchived(member);
		assertEquals("Thelmius", memberArchived.getFirstName());
		assertEquals("Aquaddapodox", memberArchived.getLastName());
	}
}
