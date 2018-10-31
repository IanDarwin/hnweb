package model;

import java.lang.reflect.InvocationTargetException;
import javax.persistence.*;
import org.apache.commons.beanutils.*;
import java.time.LocalDate;
import lombok.*;

/**
 * Archived Member has all fields of regular member, plus archived date and reason.
 */
@ToString() 
@EqualsAndHashCode()
@Entity @Table(name="member_archived")
public class MemberArchived extends Member {
	@Column(name="archive_reason")
	private String archivedReason;
	@Column(name="archive_date")
	private LocalDate archivedDate;
	
	public MemberArchived() {
		archivedDate = LocalDate.now(); // Do this "now", avoid NPE later
		archivedReason = "Deleted";
	}

	public MemberArchived(Member m) {
		this();
		try {
			BeanUtils.copyProperties(this, m);		// (dest, src)
		} catch (IllegalAccessException|InvocationTargetException ex) {
			Error t = new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
			// t.setCause(ex);
			throw t;
		}
	}

	public String getArchivedReason() {
		return archivedReason;
	}

	public LocalDate getArchivedDate() {
		return archivedDate;
	}
}
