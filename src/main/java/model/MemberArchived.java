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
 * Archived Member has all fields of regular member, plus archived date and reason.
 */
@Entity
@Table(name="member_archived")
public class MemberArchived extends Member {
	@Column(name="archive_reason")
	private String archivedReason;
	@Column(name="archive_date")
	private LocalDate archivedDate;
	
	public MemberArchived() {
		archivedDate = LocalDate.now(); // Do this "now", avoid NPE later
		archivedReason = "Deleted";
	}

	/** Copy Constructor: copies all field but for ID */
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
		secondaryMembers = new HashSet<>();
		roles = new ArrayList<>();
	}

	public String getArchivedReason() {
		return archivedReason;
	}

	public LocalDate getArchivedDate() {
		return archivedDate;
	}
}
