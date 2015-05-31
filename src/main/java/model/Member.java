package model;

import java.util.Date;
import java.util.Set;

import javax.faces.bean.ManagedBean;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Represents one person in our database.
 * @author Ian Darwin
 */
@Entity
@ManagedBean(name="person")
@Table(name="Person")
public class Member {
	
	private long id;
	private String firstName, lastName;
	private String homePhone, cellPhone;
	private String email;
	private String address, address2;
	private String city, province, postCode;
	private Country country = Country.CA;
	private MemberType memberType;
	/** If they have an executive position */
	private String position;
	private Date expiryDate;
	/** Only if memberType is MemberType.Secondary */
	private Member masterMember;
	/** Only if this is MemberType.Family */
	private Set<Member> secondaryMembers;
	/** How they get the club newsletter */
	private NewsletterMode newsletterMode;
	
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
	public Member(String firstName, String lastName, String email) {
		this.firstName = firstName;
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

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Transient
	public String getName() {
		return getFirstName() + ' ' + getLastName();
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

	@Temporal(TemporalType.DATE)
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getFirstName())
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
	
	/**
	 * Canadian Post Codes can be entered in either case
	 * and with or without the middle space; this
	 * canonicalizes them to upper case and with the space.
	 */
	@PrePersist @PreUpdate
	public void fixupPostCode() {
		System.out.println("Person.fixupPostCode()");
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
}

