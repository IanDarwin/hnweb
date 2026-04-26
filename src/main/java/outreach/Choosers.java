package outreach;

import model.Member;

import java.util.function.Predicate;

abstract class MemberChooser implements Predicate<Member> {

	private final String name;

	/// Describes this chooser for use in a dropdown
	public String toString() {
		return name;
	}

	public MemberChooser(String name) {
		this.name = name;
	}

	// You also have to provide boolean test(Member m), of course
}

/** Select members that have email addresses on file */
class MemberHasEmail extends MemberChooser {
	MemberHasEmail() {
		super("Members with emails");
	}

	public boolean test(Member member) {
		return member.getEmail() != null;
	}
}
