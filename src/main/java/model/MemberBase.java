package model;

import action.YearCalcs;
import com.darwinsys.geo.CanadaProvince;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type",
        discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue(value="MemberBase")
@Table(name="Member")
public abstract class MemberBase {

	/////////////////////////////////////////////
	// Fields common to Member and ArchivedMember
	/////////////////////////////////////////////

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_id_gen")
    @SequenceGenerator(name = "member_id_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    @Enumerated(EnumType.STRING)
    private Country country;

    @Column(name = "email")
    protected String email;

    @Column(name = "firstname")
    protected String firstName;

    @Column(name = "lastname")
    protected String lastName;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "province")
    private String province;

    @Column(name = "\"position\"", length = 100)
    private String position;

    @Column(name = "cellphone")
    private String cellPhone;

    @Column(name = "homephone")
    private String homePhone;

    @Column(name = "memberType")
    @Enumerated(EnumType.STRING)
    protected MemberType memberType;

    @Column(name = "newslettermode")
    private Integer newsletterMode;

    /** Only if memberType is MemberType.Secondary */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "masterMember_id")
    protected Member masterMember;

    /** Only if memberType is MemberType.Family */
    @OneToMany(mappedBy="masterMember", fetch = FetchType.EAGER)
    protected Set<Member> secondaryMembers;

    @Column(name = "yearjoined")
    private Integer yearJoined;

    @Column(name = "middle")
    protected String middle;

    @Column(name = "email_trips")
    private Boolean emailTrips;

    @Column(name = "expiryYear")
    private Integer expiryYear = 0;

    @Column(name = "workphone")
    private String workPhone;

    @Column(name = "password")
    private String password;

    @Column(name = "version")
    private Integer version;

    @Column(name = "notes")
    private String notes;

    @Temporal(TemporalType.DATE)
    private LocalDate creation;

    @Temporal(TemporalType.DATE)
    private LocalDate lastModified;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<MemberTransaction> memberTransactions = new ArrayList<>();

	// CONSTRUCTORS

	/** 
	 * "A very public Person"
	 */
	public MemberBase() {
		// Everybody loves an empty constructor
	}

	///////////////////////////////////
	// Setters and Getters
	///////////////////////////////////

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public Integer getNewsletterMode() {
        return newsletterMode;
    }

    public void setNewsletterMode(Integer newsletterMode) {
        this.newsletterMode = newsletterMode;
    }

    public Integer getYearJoined() {
        return yearJoined;
    }

    public void setYearJoined(Integer yearJoined) {
        this.yearJoined = yearJoined;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

	public void setExpiryYear(Integer expiryYear) {
		this.expiryYear = expiryYear;
	}
	public Integer getExpiryYear() {
		return expiryYear;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
	public String getLastnameFirstname() {
		return String.format("%s, %s", lastName, firstName);
	}

}
