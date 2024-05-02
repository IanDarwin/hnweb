package model;

public enum MemberType {

	/** An individual member */
	Individual("Ind"),
	/** Not a member, just on mailing list */
	MailOnly("ML"),
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
