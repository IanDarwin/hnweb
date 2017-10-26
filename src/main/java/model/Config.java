package model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.*;

@Entity
public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * NEW MEMBERS who sign up on or after this date (e.g., September 1)
	 * get next year's membership included.
	 */
	public final static int RENEWAL_MONTH = 9;

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	long id;

	String clubName;

	LocalDate yearStarts, yearEnds;

	int currentMemberYear;

}
	
