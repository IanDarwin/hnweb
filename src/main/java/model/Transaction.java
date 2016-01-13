package model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A Renewal records the extension of a member's membership.
 * We use the 1992 version of Date for dateRenewed property because 
 * JPA is still out of sync with Java SE.
 */
@Entity
public class Renewal {
	private long id;
	private Member member;
	private Date dateRenewed = new Date();
	private int numYears;
	private int newExpiryYear;
	
	public Renewal() {
		// Required for JPA
	}
	
	public Renewal(Member member, int nYears) {
		this.member = member;
		this.numYears = nYears;
	}
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("unused") // For JPA
	private void setId(long id) {
		this.id = id;
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
