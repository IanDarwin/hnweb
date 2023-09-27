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

	/** Copy Constructor: copies all fields but for ID and some relationships */
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
	 * If this Member is a MemberType.Family member, then
	 * allow secondary members
	 */
	public boolean allowSecondaries() {
		return memberType == MemberType.Family;
	}

	/** 
	 * If this Member is a MemberType.Family member, this
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
		// Nothing to do at present
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

