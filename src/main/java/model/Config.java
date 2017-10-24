package model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.*;

@Entity
public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	long id;

	String clubName;

	LocalDate yearStarts, yearEnds;

	int currentMemberYear;

}
	
