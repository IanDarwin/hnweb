package data;

import model.Member;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Supplier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatisticsTest extends PersistenceTest {

    final static int CURYEAR = 2026;

    @Test
    void testStatistics() throws Exception {
        final String input = "Some StatsQuery |SELECT * from member where expiryYear > ${CURYEAR}";
        final String expectedQuery = "SELECT * from member where expiryYear > " + CURYEAR;
        StatsBean.is = new ByteArrayInputStream(input.getBytes());
        StatsBean bean = new StatsBean();
        List<StatsBean.StatsQuery> list = bean.getQueries();
        assertEquals(expectedQuery, list.get(0).query());
        EntityManager em = getEntityManager();
        bean.em = em;
        Member m = new Member("Jon", "J", "Jones", "j@j.co");
        m.setExpiryYear(2222);
        em.getTransaction().begin();
        em.persist(m);
        em.getTransaction().commit();
        em.clear();
        // Now run the query against (test) database
        List<StatsBean.Stat> stats = bean.getStatistics();
        assertEquals(1, stats.size());
        StatsBean.Stat result = stats.get(0);
        assertTrue(result.result() > 0);
    }

    	// Allow tests to run something in a transaction without so much ceremony
	private void inTx(Runnable r) {
		inTx(() -> { r.run(); return null; });
	}

	public <T> T inTx(Supplier<T> s) {
		EntityTransaction tx = getEntityManager().getTransaction();
		tx.begin();
		try {
			T result = s.get();
			tx.commit();
			return result;
		} catch (RuntimeException e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
	}

}
