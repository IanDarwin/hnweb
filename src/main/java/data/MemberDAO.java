package data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

import model.Member;
import model.MemberArchived;
import model.MemberTransaction;
import model.MemberType;
import model.TransactionReason;

import static action.YearCalcs.getCurrentMembershipYear;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 */
@Named("memberDAO")
@SessionScoped
public class MemberDAO extends GenericDAO<Member> implements Serializable {

	private static final long serialVersionUID = 380283091981L;

	Logger logger = Logger.getLogger(MemberDAO.class.getName());

	@Override
	protected String getFindAllQueryString() {
		return "SELECT m FROM Member m order by m.lastName";
	}

	@SuppressWarnings("unused") // Needed by JPA
	public MemberDAO() {
		super(Member.class);
	}

	public MemberDAO(EntityManager entityManager) {
		super(Member.class, entityManager);
	}

	//////////////// CREATE METHODS /////////////////
	
	// create() is inherited

	@Override
	void postCreate(Member instance) {
		MemberTransaction tx = new MemberTransaction(instance, 1, TransactionReason.N);
		entityManager.persist(tx);
		instance.getMemberTransactions().add(tx);
	}

	//////////////// READ (FIND) METHODS /////////////////

	public Member findById(Long id) {
		return entityManager.find(Member.class, id);
	}

	public MemberArchived findArchivedById(Long id) {
		return entityManager.find(MemberArchived.class, id);
	}

	public Optional<Member> findByEmailAndPassword(String email, String password) {
		return Optional.of(entityManager.createQuery(
						"SELECT m FROM Member m WHERE m.email = :email AND m.password = :password", Member.class)
				.setParameter("email", email)
				.setParameter("password", password)
				.getSingleResult());
	}

	/** @return The first match, or empty if there is none (names are not unique). */
	public Optional<Member> findByFirstNameAndLastName(String firstName, String lastName) {
		return entityManager.createQuery(
						"SELECT m FROM Member m WHERE m.firstName = :firstName AND m.lastName = :lastName ORDER BY m.id", Member.class)
				.setParameter("firstName", firstName)
				.setParameter("lastName", lastName)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public List<Member> findNonSecondaries() {
		return entityManager.createQuery(
				"SELECT m FROM Member m WHERE m.memberType <> :secondary ORDER BY m.lastName, m.firstName", Member.class)
				.setParameter("secondary", MemberType.Secondary)
				.getResultList();
	}

	public List<Member> findCurrent() {
		return entityManager.createQuery(
				"SELECT m FROM Member m WHERE m.expiryYear >= :curYear ORDER BY m.lastName, m.firstName", Member.class)
				.setParameter("curYear", getCurrentMembershipYear())
				.getResultList();
	}

	//////////////// UPDATE METHODS /////////////////

	// update() is inherited

	//////////////// DELETE METHODS /////////////////

	// delete() is inherited, but ours is special
	// Since Member and MemberArchive are in the same table,
	// we just have to flip the 'type' column.

	public void delete(Member m) {
		delete(m, null);
	}

	public void delete(Member m, String reason) {
		String why = (reason == null || reason.isBlank()) ? "Deleted" : reason.strip();
		System.out.println("Archiving " + m + " because: " + why);
		entityManager.flush();
		int ret = entityManager.createNativeQuery(
						"update member set type = 'MemberArchived', archivedDate = ?, archivedReason = ? where id = ?")
				.setParameter(1, LocalDate.now())
				.setParameter(2, why)
				.setParameter(3, m.getId())
				.executeUpdate();
		if (ret != 1) {
			throw new IllegalStateException("delete: Native update returned " + ret);
		}
		// Changed the type w/o telling JPA, so evict it.
		entityManager.detach(m);
	}

	// Beware the hall of the necromancer!
	/** Undelete the Member and, for a family master, the secondaries archived along with them */
	public void unDelete(MemberArchived m) {
		System.out.println("Un-archiving " + m);
		entityManager.flush();

		// A secondary can't come back before its master: it would point at a non-Member row
		Long masterId = m.getMasterMemberId();
		if (masterId != null && isArchived(masterId)) {
			throw new IllegalStateException("Undelete the master (#" + masterId + ") first");
		}

		// Family archived together with this member: same master, same archive date.
		// Read the date before it is cleared below.
		LocalDate archivedOn = m.getArchivedDate();
		var q = entityManager.createNativeQuery("select id from member where masterMember_id = ? "
				+ "and type = 'MemberArchived'" + (archivedOn != null ? " and archivedDate = ?" : ""))
				.setParameter(1, m.getId());
		if (archivedOn != null) {
			q.setParameter(2, archivedOn);
		}
		List<?> familyIds = q.getResultList();

		flipToMember(m.getId());
		entityManager.detach(m);
		for (Object o : familyIds) {
			Long famId = ((Number) o).longValue();
			var fam = entityManager.find(MemberArchived.class, famId);
			if (fam != null) {
				entityManager.detach(fam);	// evict, as in delete(): it's a Member again
			}
			flipToMember(famId);
		}
	}

	private void flipToMember(Long id) {
		int ret = entityManager.createNativeQuery(
				"update member set type = 'Member', archivedDate = null, archivedReason = null where id = ?")
				.setParameter(1, id)
				.executeUpdate();
		if (ret != 1) {
			throw new IllegalStateException("unDelete: Native update of #" + id + " returned " + ret);
		}
	}

	private boolean isArchived(Long id) {
		List<?> types = entityManager.createNativeQuery("select type from member where id = ?")
				.setParameter(1, id).getResultList();
		return !types.isEmpty() && "MemberArchived".equals(types.get(0));
	}
}

