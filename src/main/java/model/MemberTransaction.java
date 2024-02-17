package model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Do not confuse this with a JPA transaction; this is an application object:
 * A Transaction records the creation or extension of a member's membership.
 * We use the 1992 version of Date for dateRenewed property because 
 * JPA is still out of sync with Java SE.
 */
@Entity
@Table(name="transaction") // legacy + shorter in db
public class MemberTransaction implements Serializable {

	private static final long serialVersionUID = 4112042259125129308L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	@Enumerated(EnumType.STRING)
	private TransactionReason reason = TransactionReason.R;
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	@ManyToOne(cascade=CascadeType.ALL)
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
