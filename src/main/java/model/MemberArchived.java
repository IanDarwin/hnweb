package model;

import java.lang.reflect.InvocationTargetException;
import javax.persistence.*;
import org.apache.commons.beanutils.*;
import java.time.LocalDate;
import lombok.*;

/**
 * Archived Member has all fields of regular member, plus archived date and reason.
 */
@NoArgsConstructor()
@ToString() 
@EqualsAndHashCode()
@Entity @Table(name="member_archived")
public class MemberArchived extends Member {
	String archivedReason;
	LocalDate archivedDate;

	public MemberArchived(Member m) {
		try {
			BeanUtils.copyProperties(this, m);		// (dest, src)
		} catch (IllegalAccessException|InvocationTargetException ex) {
			Error t = new ExceptionInInitializerError("MemberArchived: Copy Constructor Failed: " + ex);
			// t.setCause(ex);
			throw t;
		}
		archivedDate = LocalDate.now();
		archivedReason = "Deleted";
	}

	@Column(name="archive_reason")
	public String getArchivedReason() {
		return archivedReason;
	}

	@Column(name="archive_date")
	public LocalDate(archivedDate() {
		return archivedDate;
	}
}
