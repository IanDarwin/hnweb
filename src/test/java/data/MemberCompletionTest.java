package data;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Member;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberCompletionTest {

	MemberCompleteAction target;
	ArrayList<Member> members;

	@BeforeEach
	void setUp() throws Exception {
		target = new MemberCompleteAction();
		final ArrayList<Member> members = new ArrayList<Member>();
		target.setAllMembers(members);
	}

	@Test
	void test() {
		final ArrayList<Member> members = new ArrayList<Member>();
		target.setAllMembers(members);
		assertEquals(0, target.completeMember("be").size());
		members.add(new Member("Robin", "R", "Rabbet", "robin@rabbet.com"));
		assertEquals(1, target.completeMember("be").size());
	}

}
