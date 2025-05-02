package data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

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

	@Override
	protected String getFindAllQueryString() {
		return "SELECT m FROM Member m order by m.lastName";
	}

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

	public Optional<Member> findByFirstNameAndLastName(String firstName, String lastName) {
		return Optional.of(entityManager.createQuery(
						"SELECT m FROM Member m WHERE m.firstName = :firstName AND m.lastName = :lastName", Member.class)
				.setParameter("firstName", firstName)
				.setParameter("lastName", lastName)
				.getSingleResult());
	}

	public List<Member> findNonSecondaries() {
		return entityManager.createQuery(
				"SELECT m FROM member m where m.memberType != " + MemberType.Secondary.name())
				.getResultList();
	}

	//////////////// UPDATE METHODS /////////////////

	// update() is inherited

	//////////////// DELETE METHODS /////////////////

	// delete() is inherited, but uses these:

	@Override
	public void preDelete(Member instance) {
		// Remove secondaries, if any, before primary
		for (Member sm : instance.getSecondaryMembers()) {
			entityManager.remove(entityManager.merge(sm));
		}

		// Remove transactions for this user
		for (MemberTransaction tx : instance.getMemberTransactions()) {
			entityManager.remove(entityManager.merge(tx));
		}
	}

	@Override
	protected void postDelete(Member instance) {
		entityManager.persist(new MemberArchived(instance));
	}

	// Home of the necromancer:

	/** For members only, we keep an "archived" version of some fields;
	 * this method lets us undelete, given the archivedMember's pkey.
	 * @param id The primary key of the archived member
	 */
	public void undelete(long id) {
		if (id < 1) {
			throw new IllegalArgumentException("Can't undelete id " + id);
		}
		// Dead records don't have MemberTransactions, so we can just restore
		MemberArchived instance = entityManager.find(MemberArchived.class, id);
		if (instance == null) {
			throw new IllegalArgumentException("MemberArchived " + id + " not found.");
		}
		System.out.println("Trying to necromance " + instance + " with id " + id);
		entityManager.remove(instance);
		entityManager.flush();
		entityManager.persist(new Member(instance));
		entityManager.flush();	// So it prints with updated id
		System.out.printf("Restored %s from archive!\n", instance);
	}
}

