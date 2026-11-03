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

	//////////////// UPDATE METHODS /////////////////

	// update() is inherited

	//////////////// DELETE METHODS /////////////////

	// delete() is inherited, but ours is special

	public void delete(Member m) {
		System.out.println("Archiving " + m);
		entityManager.flush();		// send any pending changes to m before we go around JPA
		var query = entityManager.createNativeQuery(
			"update member set type = 'MemberArchived', archivedDate = ? where id = ?");
		query.setParameter(1, LocalDate.now());
		query.setParameter(2, m.getId());
		int ret = query.executeUpdate();
		System.out.println("delete: Native update returned " + ret);
		// Changed the type w/o telling JPA, so evict it.
		entityManager.detach(m);
	}

	/** Undelete the Member */
	// Beware the hall of the necromancer!
	void unDelete(MemberArchived m) {

		System.out.println("Un-archiving " + m);
		entityManager.flush();
		var query = entityManager.createNativeQuery("update member set type = 'Member', archivedDate = null where id = ?");
		query.setParameter(1, m.getId());
		int ret = query.executeUpdate();
		System.out.println("unDelete: Native query returned " + ret);
		entityManager.detach(m);	// same reason as in delete(): now it is really a Member again
	}
}

