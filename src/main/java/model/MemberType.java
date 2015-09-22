package model;

public enum MemberType {

	/** An individual member */
	Individual("Ind"),
	/** A student - full-time or similar */
	Student("St"),
	/** The main entry for a Family membership */
	Family("FM"),
	/** Any additional members on a Family membership */
	Secondary("FS");
	
	private final String shortName;

	private MemberType(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return shortName;
	}
}
