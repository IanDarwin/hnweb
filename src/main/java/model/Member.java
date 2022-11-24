package model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Represents one person in our database.
 * ALL data properties are in MemberBase, so we can use them both in
 * Member and in MemberArchived.
 * Equals and hashCode() based solely on ID, so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
@SuppressWarnings("unused")
public class Member extends MemberBase {

	// For app-managed security - only Members, not MemberArchived elements, can be in roles
	@ElementCollection
	protected List<String> roles = new ArrayList<>();
	
	// Member-only fields
	/** Only if memberType is MemberType.Secondary */
	@ManyToOne(fetch=FetchType.EAGER)
	protected Member masterMember;
	/** Only if memberType is MemberType.Family */
	@OneToMany(mappedBy="masterMember")
	protected Set<Member> secondaryMembers;
	
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

	/** Copy Constructor: copies all field but for ID */
	public Member(MemberArchived m) {
		this();
		try {
			BeanUtils.copyProperties(this, m);		// (destination, src)
		} catch (IllegalAccessException|InvocationTargetException ex) {
			throw new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
		}
		// Now have to clear some fields.
		id = 0;
		transactions = new ArrayList<>();
		secondaryMembers = new HashSet<>();
		roles = new ArrayList<>();
	}
	
	/** 
	 * If this Member is a MemberType.Secondary member, this
	 * Property will refer to the Master (e.g., paying!) member.
	 * @return The master member
	 */
	public Member getMasterMember() {
		return masterMember;
	}

	public void setMasterMember(Member masterMember) {
		this.masterMember = masterMember;
		if (masterMember != null) {
			masterMember.getSecondaryMembers().add(this);
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
		fixPostCode();
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
	public void fixPostCode() {
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

}

