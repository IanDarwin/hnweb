package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "numyears", nullable = false)
    private Integer numyears;

    @Column(name = "daterenewed")
    private LocalDate daterenewed;

    @Column(name = "newexpiryyear")
    private Integer newexpiryyear;

    @Column(name = "paymenttype")
    private String paymenttype;

    @Column(name = "reason")
    private String reason;

    @Column(name = "amount", precision = 5, scale = 2)
    private BigDecimal amount;

    @Column(name = "date")
    private LocalDate date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Integer getNumyears() {
        return numyears;
    }

    public void setNumyears(Integer numyears) {
        this.numyears = numyears;
    }

    public LocalDate getDaterenewed() {
        return daterenewed;
    }

    public void setDaterenewed(LocalDate daterenewed) {
        this.daterenewed = daterenewed;
    }

    public Integer getNewexpiryyear() {
        return newexpiryyear;
    }

    public void setNewexpiryyear(Integer newexpiryyear) {
        this.newexpiryyear = newexpiryyear;
    }

    public String getPaymenttype() {
        return paymenttype;
    }

    public void setPaymenttype(String paymenttype) {
        this.paymenttype = paymenttype;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
