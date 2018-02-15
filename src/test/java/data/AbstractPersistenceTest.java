package data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractPersistenceTest {

	protected static EntityManagerFactory emf;
	protected EntityManager em;

	@BeforeClass
	public static void preClass() {
		System.out.println("AbstractPersistenceTest.preClass()");
		emf = Persistence.createEntityManagerFactory("test");
	}


	@Before
	public void preTest() {
		System.out.println("AbstractPersistenceTest.preTest()");
		em = emf.createEntityManager();
	}

}
