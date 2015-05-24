package model;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * A Renewal records the extension of a member's membership
 */
@Entity
public class Renewal {
	long id;
	Person member;
	Date renewalDate;
	int nYears;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@ManyToOne
	public Person getMember() {
		return member;
	}
	public void setMember(Person member) {
		this.member = member;
	}
	public Date getRenewalDate() {
		return renewalDate;
	}
	public void setRenewalDate(Date renewalDate) {
		this.renewalDate = renewalDate;
	}
	public int getnYears() {
		return nYears;
	}
	public void setnYears(int nYears) {
		this.nYears = nYears;
	}
}
