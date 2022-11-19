package model;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Archived Member has all non-relationship fields of regular member, plus archived date and reason.
 */
@Entity
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
		// Now have to clear some fields. For now.
		id = 0;
		transactions = new ArrayList<>();
	}

	public String getArchivedReason() {
		return archivedReason;
	}

	public LocalDate getArchivedDate() {
		return archivedDate;
	}
}
