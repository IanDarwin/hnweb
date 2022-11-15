package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/**
 * Represents one person in our database.
 * ALL data properties are in MemberBase so we can use them both in
 * Member and in MemberArchived.
 * Equals and hashCode() based solely on ID so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
public class Member extends MemberBase {

	// For app-managed security - only Members, not Archived, can be in roles
	@ElementCollection
	protected List<String> roles = new ArrayList<>();
	
	// Member-only fields
	/** Only if memberType is MemberType.Secondary */
	@ManyToOne(fetch=FetchType.EAGER)
	protected Member masterMember;
	/** Only if memberType is MemberType.Family */
	@OneToMany(mappedBy="masterMember")
	protected Set<Member> secondaryMembers;
	
	// Modifiable for testing only; used in calculations
	@Transient
	private LocalDate now = LocalDate.now();
	
	/** 
	 * "A very public Person"
	 */
	public Member() {
		// Everybody loves an empty constructor
	}

	/**
	 * This constructor is likely only useful in testing
	 * @param firstName First Name
	 * @param lastName Last Name
	 * @param email Email address
	 */
	public Member(String firstName, String middle, String lastName, String email) {
		this.firstName = firstName;
		this.middle = middle;
		this.lastName = lastName;
		this.email = email;
	}
	
	/** 
	 * If this Member is a MemberType.Secondary member, this
	 * Property will refer to the Master (e.g., paying!) member.
	 * @return The master member
	 */
	public MemberBase getMasterMember() {
		return masterMember;
	}

	public void setMasterMember(Member masterMember) {
		this.masterMember = masterMember;
		if (masterMember != null) {
			masterMember.getSecondaryMembers().add((Member)this);
		}
	}

	/** 
	 * OTOH, if this Member is a MemberType.Family member, this
	 * Property will refer to the Secondary member(s).
	 * @return The secondary members
	 */
	public Set<Member> getSecondaryMembers() {
		if (secondaryMembers == null) {
			secondaryMembers = new HashSet<>();
		}
		return secondaryMembers;
	}

	public void setSecondaryMembers(Set<Member> secondaryMembers) {
		this.secondaryMembers = secondaryMembers;
	}

	@Transient
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFirstName());
		String mi = getMiddle();
		if (mi != null && mi.length() > 0) {
			sb.append(' ').append(mi);
		}
		sb.append(' ').append(getLastName());
		return sb.toString();
	}
	
	@Transient
	public String getNames() {
		return String.format("%s, %s", lastName, firstName);
	}

	@PrePersist @PreUpdate
	public void preJpa() {
		fixMembership();
		fixupPostCode();
	}
	
	/** 
	 * Only a Family membership "Secondary" can have a
	 * master member, but the UI lets them be set...
	 */
	public void fixMembership() {
		if (memberType == null) {
			throw new NullPointerException("Saving Member w/ no MemberType");
		}
		if (MemberType.Secondary != memberType) {
			masterMember = null;
		}
	}
	/**
	 * Canadian Post Codes can be entered in either case
	 * and with or without the middle space; this
	 * canonicalizes them to upper case and with the space.
	 */
	public void fixupPostCode() {
		if (getPostCode() == null) {
			return;
		}
		if (country == Country.CA) {
			postCode = getPostCode().toUpperCase();
			switch(postCode.length()) {
			case 7: // OK
				break;
			case 6: // Missing internal space
				postCode =
					postCode.substring(0,3) + " " +
					postCode.substring(3);
				break;
			default:
				throw new IllegalStateException(
					postCode + " not valid Canadian postcode");
			}
		}
	}

	// For app-managed security

	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public void addRole(String role) {
		roles.add(role);
	}

	/** Return true iff this member (or their Master Member) is expired */
	public boolean isExpired() {
		if (memberType == MemberType.Secondary) {
			if (masterMember == null) {
				System.err.printf("Member %s has no Master Member!\n", this);
				return true;	// flag it for attention!
			}
			return masterMember.isExpired();
		}

		// If current year is past expiry year, obv. this member is expired.
		if (now.getYear() > getExpiryYear()) {
			return true;
		}
		// Not currently greater than exp year, so might be in it, must
		// check if we are currently past the expiry month.
		if (now.getYear() == getExpiryYear() &&
				now.getMonth().getValue() > Config.RENEWAL_MONTH)
			return true;
		// Otherwise we are not yet in the expiry year.
		return false;
	}

	public void setNow(LocalDate now) {
		this.now = now;
	}
}

