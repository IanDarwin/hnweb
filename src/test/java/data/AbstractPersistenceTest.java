package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AbstractPersistenceTest {

	protected static EntityManagerFactory emf;
	protected EntityManager entityManager;

	// Has to be BeforeEach to protect against intra-test gunk in the database
	@BeforeEach
    void preTest0() {
		System.out.println("AbstractPersistenceTest.preClass()");
		emf = Persistence.createEntityManagerFactory("test");
	}

	// Each test deserves an EntityManager
	@BeforeEach
	void preTest1() {
		System.out.println("AbstractPersistenceTest.preTest()");
		entityManager = emf.createEntityManager();
	}

	public EntityManager getEntityManager() {
		if (entityManager == null) {
			throw new IllegalStateException("getEntityManager() called before @Before method, how?");
		}
		return entityManager;
	}

}
