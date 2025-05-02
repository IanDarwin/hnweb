package data;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.*;

import java.io.Serializable;
import java.util.List;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * MUST be called from an @Transactional business method,
 * or for testing only, use the constructor with an EntityManager
 * and manage JPA transactions yourself.
 *
 * To use:
 * * Subclass with a type parameter
 * * Provide a constructor that passes the same type as a Class ref
 * * Override getFindAllQueryString() if any params/options needed
 * * Implement any of the pre- and post- methods needed.
 * * Ensure called from a @Transactional-annotate method!
 */
@Named("memberDAO")
@SessionScoped
public abstract class GenericDAO<T> implements Serializable {

	private final Class<?> targetClass; // Both as T and as constr arg0
	protected String getFindAllQueryString() {
		return String.format("SELECT o FROM %s o", targetClass.getSimpleName());
	};

	@PersistenceContext
	protected EntityManager entityManager;

	protected GenericDAO(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	/** Allow passing {@link EntityManager}, for testing */
	public GenericDAO(Class<?> targetClass, EntityManager entityManager) {
		this(targetClass);
		this.entityManager = entityManager;
	}

	//////////////// CREATE METHODS /////////////////
	
	/**
	 * Save a new member into the database.
	 */
	public void create(T instance) {
		entityManager.persist(instance);
		postCreate(instance);
	}

	// No pre-create; just tailer the object before calling create().

	void postCreate(T instance) {
		// Hook for subclasses, eg add MemberTX to Member while in JPA tx
	}

	//////////////// READ (FIND) METHODS /////////////////

	public T findById(Long id) {
		return (T) entityManager.find(targetClass, id);
	}

	public List<T> findAll() {
		return (List<T>) entityManager.createQuery(getFindAllQueryString(), targetClass)
				.getResultList();
	}

	// Write any other query methods you need

	//////////////// UPDATE METHODS /////////////////

	public void update(Member instance) {
		entityManager.merge(instance); // in case detached - shouldn't be!
		entityManager.flush();
	}

	//////////////// DELETE METHODS /////////////////

	void preDelete(T instance) {
		// Hook for subclass
	}

	public void delete(T instance) {
		preDelete(instance);

		// Finally remove the member instance
		entityManager.remove(instance);

		postDelete(instance);
	}

	void postDelete(T instance) {
		// Hook for subclass
	}
}

