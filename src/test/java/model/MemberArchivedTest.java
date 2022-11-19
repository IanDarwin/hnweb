package model;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class MemberArchivedTest {

	Member member;
	
	@Before
	public void testCopyConstructor() {
		member = new Member();
		member.setFirstName("Thelmius");
		member.setLastName("Aquaddapodox");
		var memberArchived = new MemberArchived(member);
		assertEquals("Thelmius", memberArchived.getFirstName());
		assertEquals("Aquaddapodox", memberArchived.getLastName());
	}
}
