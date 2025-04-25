package model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.Model;
import jakarta.persistence.*;
import jakarta.inject.Named;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Represents one person in our database.
 * Both Member and MemberArchived "include" (@Embedded) MemberFields.
 * Equals and hashCode() based solely on ID, so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
@Named("person")
@Model
@Access(AccessType.FIELD)
public class Member extends MemberBase {

	////////////////////////////////////////////////
	// Member-only fields
	// i.e., not needed in MemberArchived.
	////////////////////////////////////////////////
	List<MemberTransaction> transactions;

	// For app-managed security
	@ManyToMany(fetch = FetchType.EAGER) // Or FetchType.EAGER
	@JoinColumn(name = "role_id") // Assuming 'role_id' is the FK column
	protected List<MemberRole> roles = new ArrayList<>();

	/** Only if memberType is MemberType.Secondary */
	@ManyToOne()
	protected Member masterMember;
	/** Only if memberType is MemberType.Family */
	@OneToMany(mappedBy="masterMember")
	protected Set<Member> secondaryMembers;

	protected String notes;

	/*
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
	public Member(MemberBase m) {
		this();
		try {
			BeanUtils.copyProperties(this, m);		// (destination, src)
		} catch (IllegalAccessException|InvocationTargetException ex) {
			throw new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
		}
		// Now have to clear some fields.
		id = 0L;
		transactions = new ArrayList<>();
		secondaryMembers = new HashSet<>();
		roles = new ArrayList<>();
	}

	/**
	 * If this Member is a MemberType.Secondary member, this
	 * Property will refer to the Master (e.g., paying!) member.
	 * Else null (so could use Optional here?)
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

	public List<MemberTransaction> getMemberTransactions() {
		return transactions;
	}
	public void setMemberTransactions(List<MemberTransaction> transactions) {
		this.transactions = transactions;
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
		if (memberType != MemberType.Family) {
			// Consider throwing exception instead of just printing
			System.out.println("ERROR: requested secondary members on non-family " + toString());
		}
		if (secondaryMembers == null) {
			secondaryMembers = new HashSet<>();
		}
		return secondaryMembers;
	}

	public void setSecondaryMembers(Set<Member> secondaryMembers) {
		this.secondaryMembers = secondaryMembers;
	}

	// For app-managed security

	public List<MemberRole> getRoles() {
		return roles;
	}
	public void setRoles(List<MemberRole> roles) {
		this.roles = roles;
	}

	public void addRole(String roleName) {
		roles.add(new MemberRole(roleName));
	}

	public void addRole(MemberRole role) { roles.add(role); }

}

