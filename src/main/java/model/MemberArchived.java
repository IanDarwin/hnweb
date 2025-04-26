package model;

import jakarta.persistence.*;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

@Entity
@Table(name = "member_archived")
public class MemberArchived {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "email")
    private String email;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "postcode")
    private String postCode;

    @Column(name = "province")
    private String province;

    @Column(name = "\"position\"", length = 100)
    private String position;

    @Column(name = "cellphone")
    private String cellPhone;

    @Column(name = "homephone")
    private String homePhone;

    @Column(name = "memberType")
    private String memberType;

    @Column(name = "newslettermode")
    private Integer newsletterMode;

    @Column(name = "masterMember_id")
    private Long masterMemberId;

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

    @Column(name = "username", length = 20)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "version")
    private Integer version;

    @Column(name = "archive_date")
    private LocalDate archivedDate;

    @Column(name = "archive_reason")
    private String archivedReason;

    @Column(name = "notes")
    private String notes;

    public MemberArchived(String reason) {
        archivedDate = LocalDate.now(); // Do this "now", avoid NPE later
        archivedReason = reason;
    }

    public MemberArchived() {
        this("Deleted");
    }

    /** Copy Constructor: copies all non-relationship fields but for ID */
    public MemberArchived(Member m) {
        this();
        try {
            BeanUtils.copyProperties(this, m);      // (dest, src)
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error t = new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
            // t.setCause(ex);
            throw t;
        }
        // Now have to clear the ID field
        id = 0L;
    }


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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
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

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public Integer getNewsletterMode() {
        return newsletterMode;
    }

    public void setNewsletterMode(Integer newsletterMode) {
        this.newsletterMode = newsletterMode;
    }

    public Long getMasterMemberId() {
        return masterMemberId;
    }

    public void setMasterMemberId(Long masterMemberId) {
        this.masterMemberId = masterMemberId;
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

    public Boolean getEmailTrips() {
        return emailTrips;
    }

    public void setEmailTrips(Boolean emailTrips) {
        this.emailTrips = emailTrips;
    }

    public Integer getExpiryYear() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public LocalDate getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(LocalDate archivedDate) {
        this.archivedDate = archivedDate;
    }

    public String getArchivedReason() {
        return archivedReason;
    }

    public void setArchivedReason(String archivedReason) {
        this.archivedReason = archivedReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
