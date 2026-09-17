package model;

import java.io.Serializable;
import java.util.Date;

import jakarta.enterprise.inject.Model;
import jakarta.persistence.*;

/**
 * Do not confuse this with a JPA transaction; this is an application object:
 * A Transaction records the creation or extension of a member's membership.
 * We use the 1992 version of Date for dateRenewed property because 
 * JPA is still out of sync with Java SE.
 */
@Entity
@Model
@Table(name="transaction") // legacy + shorter in db
public class MemberTransaction implements Serializable {

	private static final long serialVersionUID = 4112042259125129308L;
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_id_gen")
    @SequenceGenerator(name = "member_id_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
	@Id
	private long id;
	@Enumerated(EnumType.STRING)
	private TransactionReason reason = TransactionReason.R;
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	@ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="member_id")
	private Member member;
	@Temporal(TemporalType.DATE)
	@Column(name="date")
	private Date dateRenewed = new Date();
	private int numYears;
	private int newExpiryYear;
	
	public MemberTransaction() {
		// Required for JPA
	}
	
	public MemberTransaction(Member member, int nYears, TransactionReason transactionReason, PaymentType paymentType) {
		this.member = member;
		this.numYears = nYears;
		this.reason = transactionReason;
		this.paymentType = paymentType;
	}

	public MemberTransaction(Member member, int nYears, TransactionReason transactionReason) {
		this(member, nYears, transactionReason, null);
	}
	
	public long getId() {
		return id;
	}
	
	public TransactionReason getReason() {
		return reason;
	}
	public void setReason(TransactionReason reason) {
		this.reason = reason;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}

	public Date getDateRenewed() {
		return dateRenewed;
	}
	public void setDateRenewed(Date renewalDate) {
		this.dateRenewed = renewalDate;
	}

	public int getNumYears() {
		return numYears;
	}
	public void setNumYears(int nYears) {
		this.numYears = nYears;
	}

	public int getNewExpiryYear() {
		return newExpiryYear;
	}
	public void setNewExpiryYear(int year) {
		this.newExpiryYear = year;
	}
}
