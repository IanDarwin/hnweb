package model;

import action.YearCalcs;
import com.darwinsys.geo.CanadaProvince;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// @MappedSuperclass
// @Access(AccessType.FIELD)
public abstract class MemberBase {

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
	@Temporal(TemporalType.DATE)
	// @Transient
	protected LocalDate lastModified;
	
	@Enumerated(EnumType.STRING)
	protected MemberType memberType = MemberType.Individual;

	protected Integer yearJoined = LocalDate.now().getYear();
	@Column(name="email_trips",
	         columnDefinition="boolean default false")
	protected boolean emailTrips;
	@Version // Field only used for optimistic locking
	protected int version;
	
	/** 
	 * "A very public Person"
	 */
	public MemberBase() {
		// Everybody loves an empty constructor
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
	
	public boolean isEmailTrips() {
		return emailTrips;
	}
	public void setEmailTrips(boolean emailTrips) {
		this.emailTrips = emailTrips;
	}

	///////////////////////////////////
	// Methods for all types of members
	///////////////////////////////////

	public void acceptRenewal(MemberTransaction renewal) {
		setExpiryYear(YearCalcs.getRenewedExpiryYear(getExpiryYear(), renewal.getNumYears(), LocalDate.now()));
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

}
