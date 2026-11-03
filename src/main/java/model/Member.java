package model;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Named;
import jakarta.persistence.*;
import org.apache.commons.beanutils.BeanUtils;

import action.YearCalcs;

/**
 * Represents one person in the club's database.
 * Still has too much hand-coded logic, alas.
 */
@Entity
@Named("person")
@DiscriminatorValue("Member")
public class Member extends MemberBase{

	private static final long serialVersionUID = 92899228338972L;

	// CONSTRUCTOS
    public Member() {
        // empty
    }

    /**
     * Constructor for MemberHome and for testing
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

    // COMMON METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('#').append(getId()).append(' ');
        sb.append(getFirstName())
                .append(' ')
                .append(getLastName());
        sb.append(' ')
                .append('<')
                .append(getEmail())
                .append('>');
        return sb.toString();
    }

	// OTHER METHODS

    // For member relationships
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

    public void acceptRenewal(MemberTransaction renewal) {
        acceptRenewal(renewal, LocalDate.now());
    }

    /** As {@link #acceptRenewal(MemberTransaction)} but as of a given date, for testing. */
    public void acceptRenewal(MemberTransaction renewal, LocalDate now) {
        setExpiryYear(YearCalcs.getRenewedExpiryYear(getExpiryYear(), renewal.getNumYears(), now));
        renewal.setNewExpiryYear(getExpiryYear());
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

    public List<MemberTransaction> getMemberTransactions() {
        return memberTransactions;
    }

	@Transient
	public boolean isExecutive() {
		return getPosition() != null && getPosition().length() > 0;
	}

}
