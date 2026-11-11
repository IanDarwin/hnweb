package data;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Supplier;

import jakarta.persistence.EntityTransaction;
import model.Member;
import model.MemberArchived;
import model.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Archiving ("deleting") and restoring members, especially families: a Family master with
 * Secondary members. Archive then restore must put everything back as it was.
 * <p>
 * Checks the database directly with native SQL wherever it can, because the "archive" is a
 * native UPDATE of the discriminator column that JPA does not know about, and the persistence
 * context would otherwise hand us stale objects.
 */
class MemberArchiveRestoreTest extends AbstractPersistenceTest {

	MemberDAO memberDAO;
	Member master, sec1, sec2;

	@BeforeEach
	void setUpFamily() {
		memberDAO = new MemberDAO(getEntityManager());

		master = new Member("Mary", "Q", "Family", "mary@example.test");
		master.setMemberType(MemberType.Family);
		master.setExpiryYear(2030);

		sec1 = new Member("Sam", null, "Family", null);
		sec1.setMemberType(MemberType.Secondary);
		sec1.setMasterMember(master);

		sec2 = new Member("Sue", null, "Family", "sue@example.test");
		sec2.setMemberType(MemberType.Secondary);
		sec2.setMasterMember(master);

		inTx(() -> {
			memberDAO.create(master);
			memberDAO.create(sec1);
			memberDAO.create(sec2);
		});
		getEntityManager().clear();
	}

	// ----- tests -----

	@Test
	void archivingAMasterArchivesTheSecondariesToo() {
		archiveFamily();

		assertEquals("MemberArchived", typeOf(master.getId()));
		assertEquals("MemberArchived", typeOf(sec1.getId()));
		assertEquals("MemberArchived", typeOf(sec2.getId()));
		assertEquals(0, getEntityManager().createQuery("select m from Member m", Member.class).getResultList().size());
		assertEquals(3, getEntityManager().createQuery("select m from MemberArchived m", MemberArchived.class).getResultList().size());
	}

	@Test
	void restoringAMasterRestoresTheFamily() {
		archiveFamily();

		inTx(() -> memberDAO.unDelete(memberDAO.findArchivedById(master.getId())));
		getEntityManager().clear();

		// All three are active again, with the archive details cleared...
		for (Member m : new Member[] { master, sec1, sec2 }) {
			assertEquals("Member", typeOf(m.getId()), m.getName());
			assertEquals(1, countWhere("id = ?1 and archiveddate is null and archivedreason is null", m.getId()), m.getName());
		}
		// ...and the family is wired together just as it was
		Member again = memberDAO.findById(master.getId());
		assertEquals("Mary", again.getFirstName());
		assertEquals(2030, again.getExpiryYear());
		assertEquals(2, again.getSecondaryMembers().size(), "secondaries should be back in the master's set");
		for (Member s : again.getSecondaryMembers()) {
			assertEquals(again, s.getMasterMember());
			assertEquals(MemberType.Secondary, s.getMemberType());
		}
	}

	@Test
	void restoringIsIdempotentWithRespectToTheMemberList() {
		archiveFamily();
		inTx(() -> memberDAO.unDelete(memberDAO.findArchivedById(master.getId())));
		getEntityManager().clear();

		var members = getEntityManager().createQuery("select m from Member m order by m.id", Member.class).getResultList();
		assertEquals(3, members.size());
		assertEquals(0, getEntityManager().createQuery("select m from MemberArchived m", MemberArchived.class).getResultList().size());
	}

	@Test
	void aSecondaryArchivedOnAnotherDayStaysArchivedWhenTheMasterIsRestored() {
		archiveFamily();
		// sec2 was in fact archived a month earlier, on its own
		inTx(() -> getEntityManager().createNativeQuery("update member set archiveddate = ?1 where id = ?2")
				.setParameter(1, LocalDate.now().minusDays(30))
				.setParameter(2, sec2.getId())
				.executeUpdate());
		getEntityManager().clear();

		inTx(() -> memberDAO.unDelete(memberDAO.findArchivedById(master.getId())));
		getEntityManager().clear();

		assertEquals("Member", typeOf(master.getId()));
		assertEquals("Member", typeOf(sec1.getId()));
		assertEquals("MemberArchived", typeOf(sec2.getId()), "not part of this archive, so not restored with it");
	}

	@Test
	void aSecondaryCannotBeRestoredWhileItsMasterIsStillArchived() {
		archiveFamily();

		MemberArchived archivedSecondary = memberDAO.findArchivedById(sec1.getId());
		assertThrows(IllegalStateException.class, () -> inTx(() -> memberDAO.unDelete(archivedSecondary)));
		getEntityManager().clear();

		assertEquals("MemberArchived", typeOf(sec1.getId()));
		assertEquals("MemberArchived", typeOf(master.getId()));
	}

	@Test
	void aSecondaryArchivedAloneCanBeRestoredWhileTheMasterIsActive() {
		// Archive just sec1, the way an admin would by deleting one family member
		inTx(() -> memberDAO.delete(memberDAO.findById(sec1.getId())));
		getEntityManager().clear();

		assertEquals("MemberArchived", typeOf(sec1.getId()));
		assertEquals(1, memberDAO.findById(master.getId()).getSecondaryMembers().size(),
				"only the active secondary should show in the master's set");
		getEntityManager().clear();

		inTx(() -> memberDAO.unDelete(memberDAO.findArchivedById(sec1.getId())));
		getEntityManager().clear();

		assertEquals("Member", typeOf(sec1.getId()));
		Member again = memberDAO.findById(sec1.getId());
		assertEquals(master.getId(), again.getMasterMember().getId());
		assertEquals(2, memberDAO.findById(master.getId()).getSecondaryMembers().size());
	}

	@Test
	void restoringAMemberWhoIsNotArchivedIsAnError() {
		// MemberDAO.unDelete takes a MemberArchived, so the way to get this wrong is a stale object
		archiveFamily();
		MemberArchived stale = memberDAO.findArchivedById(sec1.getId());
		inTx(() -> getEntityManager().createNativeQuery("update member set type = 'Member' where id = ?1")
				.setParameter(1, sec1.getId()).executeUpdate());

		// The row is no longer archived: restoring it must not silently report success
		assertThrows(RuntimeException.class, () -> inTx(() -> memberDAO.unDelete(stale)));
	}

	// ----- helpers -----

	/** Archive the master and every secondary, the way MemberHome.remove() does. */
	private void archiveFamily() {
		inTx(() -> {
			Member m = memberDAO.findById(master.getId());
			for (Member s : new ArrayList<>(m.getSecondaryMembers())) {
				memberDAO.delete(s);
			}
			memberDAO.delete(m);
		});
		getEntityManager().clear();
	}

	/** The discriminator column straight from the database, bypassing JPA's cache */
	private String typeOf(Long id) {
		return (String) getEntityManager().createNativeQuery("select type from member where id = ?1")
				.setParameter(1, id)
				.getSingleResult();
	}

	private long countWhere(String where, Object... params) {
		var q = getEntityManager().createNativeQuery("select count(*) from member where " + where);
		for (int i = 0; i < params.length; i++) {
			q.setParameter(i + 1, params[i]);
		}
		return ((Number) q.getSingleResult()).longValue();
	}

	// Stand-ins for the container's @Transactional: the DAO must be called inside a transaction
	private void inTx(Runnable r) {
		inTx(() -> { r.run(); return null; });
	}

	private <T> T inTx(Supplier<T> s) {
		EntityTransaction tx = getEntityManager().getTransaction();
		tx.begin();
		try {
			T result = s.get();
			tx.commit();
			return result;
		} catch (RuntimeException e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
	}
}
