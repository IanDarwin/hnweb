package model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.Model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;

import com.darwinsys.geo.CanadaProvince;
import action.YearCalcs;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Represents one person in our database.
 * ALL data properties are here, so we can use them both in
 * Member and in MemberArchived.
 * Equals and hashCode() based solely on ID, so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
@SuppressWarnings("unused")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Named("person")
@Model
public class Member {

	/////////////////////////////////////////////
	// Fields common to Member and ArchivedMember
	/////////////////////////////////////////////
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected Long id; // Should be private but Archived copy-constructor needs it.
	protected String email;
	protected String password;
	protected String firstName, middle, lastName;
	protected String homePhone, cellPhone, workPhone;
	protected String city;
	@Enumerated(EnumType.STRING)
	protected CanadaProvince province;
	@Enumerated(EnumType.STRING)
	protected Country country = Country.CA;
	/** If they have an executive position */
	protected String position;
	/** 4-digit year */
	protected int expiryYear;
	@Basic(optional = false)
	// This is an obvious fix, presumable a newer JPA will fix it
	// @Temporal(TemporalType.DATE)
	@Transient
	protected LocalDate lastModified;
	
	@Enumerated(EnumType.STRING)
	protected MemberType memberType = MemberType.Individual;

	protected Integer yearJoined = LocalDate.now().getYear();
	@Column(name="email_trips",
	         columnDefinition="boolean default false")
	protected boolean emailTrips;
	@Version // Field only used for optimistic locking
	protected int version;
	@OneToMany(mappedBy="member", cascade=CascadeType.ALL)
	protected List<MemberTransaction> transactions = new ArrayList<>();
	
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

	public long getId() {
		return id;
	}
	// And no setId please.

	/** Accessor for the password. May be hashed or not; the semantics
	 * are provided by the external control implementation, not coded here.
	 * @return The password string
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddle() {
		return middle;
	}

	public void setMiddle(String middle) {
		this.middle = middle;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public MemberType getMemberType() {
		return memberType;
	}

	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}

	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	public String getWorkPhone() {
		return workPhone;
	}
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public CanadaProvince getProvince() {
		return province;
	}

	public void setProvince(CanadaProvince province) {
		this.province = province;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	@Transient
	public boolean isExecutive() {
		return getPosition() != null && getPosition().length() > 0;
	}

	public int getExpiryYear() {
		return expiryYear;
	}

	public void setExpiryYear(int expiryYear) {
		this.expiryYear = expiryYear;
	}
	
	public void setYearJoined(Integer yearJoined) {
		this.yearJoined = yearJoined;
	}
	public Integer getYearJoined() {
		return yearJoined;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}
	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public List<MemberTransaction> getMemberTransactions() {
		return transactions;
	}
	public void setMemberTransactions(List<MemberTransaction> transactions) {
		this.transactions = transactions;
	}

	public boolean isEmailTrips() {
		return emailTrips;
	}
	public void setEmailTrips(boolean emailTrips) {
		this.emailTrips = emailTrips;
	}


	// For app-managed security - only Members, not MemberArchived elements, can be in roles
	@ElementCollection()
	protected List<MemberRole> roles = new ArrayList<>();

	////////////////////////////////////////////////
	// Member-only fields
	// i.e., not needed in MemberArchived.
	////////////////////////////////////////////////

	/** Only if memberType is MemberType.Secondary */
	@ManyToOne()
	protected Member masterMember;
	/** Only if memberType is MemberType.Family */
	@OneToMany(mappedBy="masterMember")
	protected Set<Member> secondaryMembers;

	protected String notes;

	/** Copy Constructor: copies all fields but for ID and some relationships */
	public Member(MemberArchived m) {
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

	///////////////////////////////////
	// Methods for all types of members
	///////////////////////////////////

	public void acceptRenewal(MemberTransaction renewal) {
		setExpiryYear(YearCalcs.getCurrentMembershipYear() + renewal.getNumYears());
		renewal.setNewExpiryYear(getExpiryYear());
	}

	@PreUpdate @PrePersist
	public void updateLastModified() {
		lastModified = LocalDate.now();
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/**
	 * NOTE! This is non-standard:
	 * Only compares on ID, AND
	 * No Entity can be equal to 'no entity' (id 0).
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		// Compare two Members or two MemberArchived, etc.
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (id == 0 || other.getId() == 0) {
			return false;
		}
		return getId() == other.getId();
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

