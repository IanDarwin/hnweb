package model;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;

import jakarta.enterprise.inject.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Archived Member has all non-relationship fields of regular member, plus archived date and reason.
 */
@Entity
@Model
@Table(name="member_archived")
public class MemberArchived extends MemberBase {
	@Column(name="archive_reason")
	private String archivedReason;
	@Column(name="archive_date")
	private LocalDate archivedDate;
	
	public MemberArchived(String reason) {
		archivedDate = LocalDate.now(); // Do this "now", avoid NPE later
		archivedReason = reason;
	}

	public MemberArchived() {
		this("Deleted");
	}

	/** Copy Constructor: copies all non-relationship fields but for ID */
	public MemberArchived(Member m) {
		this();
		try {
			BeanUtils.copyProperties(this, m);		// (dest, src)
		} catch (IllegalAccessException|InvocationTargetException ex) {
			Error t = new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
			// t.setCause(ex);
			throw t;
		}
		// Now have to clear the ID field
		id = 0L;
	}

	public String getArchivedReason() {
		return archivedReason;
	}

	public LocalDate getArchivedDate() {
		return archivedDate;
	}
}
