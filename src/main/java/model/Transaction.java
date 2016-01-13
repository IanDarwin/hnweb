package model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Do not confuse this with a JPA transaction; this is an application object:
 * A Transaction records the creation or extension of a member's membership.
 * We use the 1992 version of Date for dateRenewed property because 
 * JPA is still out of sync with Java SE.
 */
@Entity
public class Transaction {
	private long id;
	private TransactionReason reason = TransactionReason.R;
	private PaymentType paymentType;
	private Member member;
	private Date dateRenewed = new Date();
	private int numYears;
	private int newExpiryYear;
	
	public Transaction() {
		// Required for JPA
	}
	
	public Transaction(Member member, int nYears, TransactionReason transactionReason) {
		this.member = member;
		this.numYears = nYears;
		this.reason = transactionReason;
	}
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("unused") // For JPA
	private void setId(long id) {
		this.id = id;
	}
	
	@Enumerated(EnumType.STRING)
	public TransactionReason getReason() {
		return reason;
	}

	public void setReason(TransactionReason reason) {
		this.reason = reason;
	}

	@Enumerated(EnumType.STRING)
	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	@ManyToOne
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
	@Temporal(TemporalType.DATE)
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
