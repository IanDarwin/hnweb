package data;

import java.sql.SQLException;
import java.util.List;

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

	// General purpose Member list printer
	public void dumpMemberTable(EntityManager em) throws SQLException {
		List<Object[]> rs = em
			.createNativeQuery("select id, type, firstname,lastname,mastermember_id,archiveddate,archivedreason from member")
			.getResultList();
		for (Object[] arr : rs) {
			System.out.printf("%5d Type %20s Member %15s %15s MasterMemberId %4d Archived %s for %s\n",
							arr[0], arr[1], arr[2], arr[3], arr[4], arr[5], arr[6]);
		}
		System.out.flush();
    }
}
