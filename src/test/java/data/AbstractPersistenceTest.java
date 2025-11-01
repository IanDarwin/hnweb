package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AbstractPersistenceTest {

	protected static EntityManagerFactory emf;
	protected EntityManager entityManager;

	@BeforeAll
	static void preClass() {
		System.out.println("AbstractPersistenceTest.preClass()");
		emf = Persistence.createEntityManagerFactory("test");
	}

	public EntityManager getEntityManager() {
		if (entityManager == null) {
			throw new IllegalStateException("getEntityManager() called before @Before method, how?");
		}
		return entityManager;
	}

	@BeforeEach
	void preTest() {
		System.out.println("AbstractPersistenceTest.preTest()");
		entityManager = emf.createEntityManager();
	}

}
