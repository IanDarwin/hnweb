package model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/**
 * Represents one person in our database.
 * Equals and hashCode() based solely on ID so we don't get new entries made
 * when editing other fields.
 * @author Ian Darwin
 */
@Entity
@ManagedBean(name="person")
public class Member {
	
	private long id;
	private String userName, password;
	private String firstName, middle, lastName;
	private String homePhone, cellPhone, workPhone;
	private String email;
	private String address, address2;
	private String city, province, postCode;
	private Country country = Country.CA;
	/** If they have an executive position */
	private String position;
	/** 4-digit year */
	private int expiryDate;
	private MemberType memberType = MemberType.Individual;
	/** Only if memberType is MemberType.Secondary */
	private Member masterMember;
	/** Only if memberType is MemberType.Family */
	private Set<Member> secondaryMembers;
	/** How they get the club newsletter */
	private NewsletterMode newsletterMode;
	private Integer yearJoined = LocalDate.now().getYear();
	private boolean emailTrips;
	
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

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	/** Accessor for the password. May be hashed or not; the sematnics
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

	@Enumerated(EnumType.STRING)
	public MemberType getMemberType() {
		return memberType;
	}

	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}

	/** 
	 * If this Member is a MemberType.Secondary member, this
	 * Property will refer to the Master (e.g., paying!) member.
	 * @return The master member
	 */
	@ManyToOne
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
	@OneToMany(mappedBy="masterMember")
	public Set<Member> getSecondaryMembers() {
		if (secondaryMembers == null) {
			secondaryMembers = new HashSet<>();
		}
		return secondaryMembers;
	}

	public void setSecondaryMembers(Set<Member> secondaryMembers) {
		this.secondaryMembers = secondaryMembers;
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

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@Enumerated(EnumType.STRING)
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public NewsletterMode getNewsletterMode() {
		return newsletterMode;
	}

	public void setNewsletterMode(NewsletterMode newsletterMode) {
		this.newsletterMode = newsletterMode;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(int expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public void setYearJoined(Integer yearJoined) {
		this.yearJoined = yearJoined;
	}

	public Integer getYearJoined() {
		return yearJoined;
	}
	
	public void setEmailTrips(boolean emailTrips) {
		this.emailTrips = emailTrips;
	}

	@Column(name="email_trips",
	         columnDefinition="boolean default false")
	public boolean isEmailTrips() {
		return emailTrips;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
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
			String ananan = getPostCode().toUpperCase();
			switch(ananan.length()) {
			case 7: // OK
				break;
			case 6: // Missing internal space
				postCode = 
					ananan.substring(0,3) + " " +
					ananan.substring(3);
				break;
			default:
				throw new IllegalStateException(
					postCode + " not valid Canadian postcode");
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/**
	 * CUIDATE! This is non-standard:
	 * Only compares on ID, AND:
	 * No Entity can be equal to 'no entity' (id 0).
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (id == 0 || other.id == 0) {
			return false;
		}
		return getId() == other.getId();
	}
}

