package model;

/**
 * How a membership was paid for. Stored by name (EnumType.STRING), so
 * never rename or remove a constant that may already be in the database.
 */
public enum PaymentType {
	CASH("Cash"),
	CHEQUE("Cheque"),
	CREDIT("Credit card"),
	DEBIT("Debit card"),
	ETRANSFER("Interac e-Transfer"),
	UNKNOWN("Unknown payment type");

	private final String label;

	PaymentType(String label) {
		this.label = label;
	}

	/** Human-readable name for drop-downs and reports. */
	public String getLabel() {
		return label;
	}
}
