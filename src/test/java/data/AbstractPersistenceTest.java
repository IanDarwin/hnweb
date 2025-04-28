package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AbstractPersistenceTest {

	protected static EntityManagerFactory emf;
	protected EntityManager em;

	@BeforeAll
	public static void preClass() {
		System.out.println("AbstractPersistenceTest.preClass()");
		emf = Persistence.createEntityManagerFactory("test");
	}

	public EntityManager getEntityManager() {
		if (em == null) {
			throw new IllegalStateException("getEntityManager() called before @Before method, how?");
		}
		return em;
	}

	@BeforeEach
	public void preTest() {
		System.out.println("AbstractPersistenceTest.preTest()");
		em = emf.createEntityManager();
	}

}
