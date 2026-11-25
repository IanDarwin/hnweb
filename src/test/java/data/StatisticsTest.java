package data;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.List;

import model.Member;
import model.MemberType;
import model.UnconfirmedMember;
import org.junit.jupiter.api.Test;

class StatisticsTest extends AbstractPersistenceTest {

	/** Passed to the bean explicitly, so these tests don't depend on today's date */
	static final int CURYEAR = 2026;

	private StatsBean beanFor(String statsDat) throws Exception {
		StatsBean.source = () -> new ByteArrayInputStream(statsDat.getBytes(UTF_8));
		StatsBean bean = new StatsBean(CURYEAR);
		bean.em = getEntityManager();
		return bean;
	}

	@Test
	void placeholdersAreFilledInBothNameAndQuery() throws Exception {
		var bean = beanFor("Paid for ${CURYEAR}, lapsed after ${LASTYEAR}|SELECT COUNT(id) FROM member WHERE expiryYear >= ${CURYEAR} AND expiryYear < ${LASTYEAR};\n");
		var q = bean.getQueries().get(0);
		assertEquals("Paid for 2026, lapsed after 2025", q.name());
		assertEquals("SELECT COUNT(id) FROM member WHERE expiryYear >= 2026 AND expiryYear < 2025", q.query());
	}

	@Test
	void everyBeanGetsTheQueries() throws Exception {
		// A bean per request: the second one must not find the file already used up
		var text = "A|SELECT COUNT(id) FROM member\n";
		assertEquals(1, beanFor(text).getQueries().size());
		assertEquals(1, beanFor(text).getQueries().size());
	}

	@Test
	void countsOnlyRealMembersNotArchivedOrUnconfirmed() throws Exception {
		Member real = member("Jon", "j@j.co");
		Member gone = member("Gail", "g@g.co");
		var unconfirmed = new UnconfirmedMember();
		unconfirmed.setFirstName("Una");
		unconfirmed.setLastName("Unconfirmed");
		unconfirmed.setEmail("u@u.co");
		unconfirmed.setMemberType(MemberType.Individual);
		unconfirmed.setExpiryYear(2222);
		inTx(() -> {
			getEntityManager().persist(real);
			getEntityManager().persist(gone);
			getEntityManager().persist(unconfirmed);
		});
		var dao = new MemberDAO(getEntityManager());
		inTx(() -> dao.delete(dao.findById(gone.getId())));
		getEntityManager().clear();

		var bean = beanFor("""
			Real paid-up members|SELECT COUNT(id) FROM member WHERE type = 'Member' AND expiryyear >= ${CURYEAR}
			Every row, any type|SELECT COUNT(id) FROM member
			Waiting to confirm|SELECT COUNT(id) FROM member WHERE type = 'Unconfirmed'
			Archived|SELECT COUNT(id) FROM member WHERE type = 'MemberArchived'
			""");
		List<StatsBean.Stat> stats = bean.getStatistics();
		assertEquals(4, stats.size());
		assertEquals(1, stats.get(0).result(), "only the real member");
		assertEquals(3, stats.get(1).result(), "so the type test matters");
		assertEquals(1, stats.get(2).result());
		assertEquals(1, stats.get(3).result());
		// The page asks more than once per request; the answer must not change or re-query
		assertEquals(stats, bean.getStatistics());
	}

	private static Member member(String first, String email) {
		Member m = new Member(first, null, "Tester", email);
		m.setMemberType(MemberType.Individual);
		m.setExpiryYear(2222);
		return m;
	}
}
