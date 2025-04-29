package data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.Member;
import model.MemberType;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Jakarta Data.
 * The methods in the inherited interface suffice for many apps!
 */
@Named("memberList")
@SessionScoped
public class MemberList implements Serializable {

	@PersistenceContext
	private EntityManager entityManager;

	public MemberList() {
		// empty
	}
	public MemberList(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Member findById(Long id) {
		return entityManager.find(Member.class, id);
	}

	public List<Member> findAll() {
		return entityManager.createQuery("SELECT m FROM Member m order by m.lastName", Member.class)
				.getResultList();
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
}

