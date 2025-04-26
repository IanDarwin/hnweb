package model;

import javax.persistence.*;
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
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "province")
    private String province;

    @Column(name = "\"position\"", length = 100)
    private String position;

    @Column(name = "cellphone")
    private String cellphone;

    @Column(name = "homephone")
    private String homephone;

    @Column(name = "membertype")
    private String membertype;

    @Column(name = "newslettermode")
    private Integer newslettermode;

    @Column(name = "mastermember_id")
    private Long mastermemberId;

    @Column(name = "yearjoined")
    private Integer yearjoined;

    @Column(name = "middle")
    private String middle;

    @Column(name = "email_trips")
    private Boolean emailTrips;

    @Column(name = "expiryyear")
    private Integer expiryyear;

    @Column(name = "workphone")
    private String workphone;

    @Column(name = "username", length = 20)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "version")
    private Integer version;

    @Column(name = "archive_date")
    private LocalDate archiveDate;

    @Lob
    @Column(name = "archive_reason")
    private String archiveReason;

    @Column(name = "notes")
    private String notes;

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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getHomephone() {
        return homephone;
    }

    public void setHomephone(String homephone) {
        this.homephone = homephone;
    }

    public String getMembertype() {
        return membertype;
    }

    public void setMembertype(String membertype) {
        this.membertype = membertype;
    }

    public Integer getNewslettermode() {
        return newslettermode;
    }

    public void setNewslettermode(Integer newslettermode) {
        this.newslettermode = newslettermode;
    }

    public Long getMastermemberId() {
        return mastermemberId;
    }

    public void setMastermemberId(Long mastermemberId) {
        this.mastermemberId = mastermemberId;
    }

    public Integer getYearjoined() {
        return yearjoined;
    }

    public void setYearjoined(Integer yearjoined) {
        this.yearjoined = yearjoined;
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

    public Integer getExpiryyear() {
        return expiryyear;
    }

    public void setExpiryyear(Integer expiryyear) {
        this.expiryyear = expiryyear;
    }

    public String getWorkphone() {
        return workphone;
    }

    public void setWorkphone(String workphone) {
        this.workphone = workphone;
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

    public LocalDate getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(LocalDate archiveDate) {
        this.archiveDate = archiveDate;
    }

    public String getArchiveReason() {
        return archiveReason;
    }

    public void setArchiveReason(String archiveReason) {
        this.archiveReason = archiveReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}