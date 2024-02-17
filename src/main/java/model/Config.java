package model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Membersship year runs from Sept 1 to Aug 31. All memberships
	 * are due to renew starting in this month.
	 */
	public final static int RENEWAL_MONTH = 9;

	/**
	 * But: "New memberships after March 1 extend to the following year"
	 */
	final static int RENEWAL_FREE_YEAR_MONTH = 3;

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	long id;

	String clubName;

	LocalDate yearStarts, yearEnds;

	int currentMemberYear;

}
	
