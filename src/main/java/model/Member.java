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
public class Member {
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
    private String email;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

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
    private MemberType memberType;

    @Column(name = "newslettermode")
    private Integer newsletterMode;

    /** Only if memberType is MemberType.Secondary */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "masterMember_id")
    private Member masterMember;

    /** Only if memberType is MemberType.Family */
    @OneToMany(mappedBy="masterMember", fetch = FetchType.EAGER)
    protected Set<Member> secondaryMembers;

    @Column(name = "yearjoined")
    private Integer yearJoined;

    @Column(name = "middle")
    private String middle;

    @Column(name = "email_trips")
    private Boolean emailTrips;

    @Column(name = "expiryYear")
    private Integer expiryYear;

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

	// For app-managed security - only Members, not MemberArchived elements, can be in roles
	@ElementCollection(fetch=FetchType.EAGER)
	private List<MemberRole> roles = new ArrayList<>();

	// CONSTRUCTORS

    public Member() {
        // empty, needed for JPA
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

    /** Necromantic Copy Constructor: copies all fields but for ID and some relationships */
    public Member(MemberArchived m) {
        this();
        try {
            BeanUtils.copyProperties(this, m);      // (destination, src)
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
        }
        // Now have to clear some fields.
        id = 0L;
        // XXX transactions = new ArrayList<>();
        secondaryMembers = new HashSet<>();
        position = null;
    }

	// COMMON METHODS

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

    @Transient
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFirstName());
        String mi = getMiddle();
        if (mi != null && !mi.isEmpty()) {
            sb.append(' ').append(mi);
        }
        sb.append(' ').append(getLastName());
        return sb.toString();
    }

    @Transient
    public String getNames() {
        return String.format("%s, %s", lastName, firstName);
    }

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
        setExpiryYear(YearCalcs.getCurrentMembershipYear() + renewal.getNumYears());
        renewal.setNewExpiryYear(getExpiryYear());
    }

    @PreUpdate @PrePersist
    public void updateLastModified() {
        lastModified = LocalDate.now();
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
        return List.of();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MemberRole> getRoles() {
        return roles;
    }
    public void setRoles(List<MemberRole> roles) {
        this.roles = roles;
    }

	public void addRole(MemberRole role) {
		roles.add(role);
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

    public Boolean isEmailTrips() {
        return emailTrips;
    }

    public void setEmailTrips(Boolean emailTrips) {
        this.emailTrips = emailTrips;
    }
	
	@Transient
	public boolean isExecutive() {
		return getPosition() != null && getPosition().length() > 0;
	}

    public Integer getExpiryYear() {
		if (expiryYear == null) {
			return 0;
		}
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
