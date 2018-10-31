package data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import model.Member;

public class MemberCompletionTest {

	MemberCompleteAction target;
	ArrayList<Member> members;
	
	@Before
	public void setUp() throws Exception {
		target = new MemberCompleteAction();
		final ArrayList<Member> members = new ArrayList<Member>();
		target.setAllMembers(members);
	}

	@Test
	public void test() {
		final ArrayList<Member> members = new ArrayList<Member>();
		target.setAllMembers(members);
		assertEquals(0, target.completeMember("be").size());
		members.add(new Member("Robin", "R", "Rabbet", "robin@rabbet.com"));
		assertEquals(1, target.completeMember("be").size());
	}

}
