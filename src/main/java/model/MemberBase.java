package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.persistence.*;

import com.darwinsys.geo.CanadaProvince;

/**
 * Represents one person in our database.
 * ALL data properties are in MemberBase, so we can use them both in
 * Equals and hashCode() based solely on ID, so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Named("person")
@SuppressWarnings("unused")
public abstract class MemberBase {
	
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected long id; // Should be private but Archived copy-constructor needs it.
	protected String email;
	protected String password;
	protected String firstName, middle, lastName;
	protected String homePhone, cellPhone, workPhone;
	protected String address, address2;
	protected String city, postCode;
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
	public MemberBase() {
		// Everybody loves an empty constructor
	}

	/**
	 * This constructor is likely only useful in testing
	 * @param firstName First Name
	 * @param lastName Last Name
	 * @param email Email address
	 */
	public MemberBase(String firstName, String middle, String lastName, String email) {
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
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

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
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

	public void acceptRenewal(MemberTransaction renewal) {
		// XXX This should be getCurrentMembershipYear() + renewal.getNumYears
		setExpiryYear(getExpiryYear() + renewal.getNumYears());
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
		if (getEmail() != null) {
			sb.append(' ')
			.append('<')
			.append(getEmail())
			.append('>');
		}
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
	 * CUIDADO! This is non-standard:
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
		MemberBase other = (MemberBase) obj;
		if (id == 0 || other.getId() == 0) {
			return false;
		}
		return getId() == other.getId();
	}
}

