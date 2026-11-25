package data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import action.YearCalcs;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * The Statistics page's data: runs each query in stats.dat and reports the counts.
 * <p>
 * stats.dat lines are "Description|SQL query"; the query must return one number (use COUNT).
 * In descriptions and queries, ${CURYEAR} is the current membership year and ${LASTYEAR} the one before.
 */
@Named
@RequestScoped
public class StatsBean {

	record StatsQuery(String name, String query) {}

	/** Public, with getters, so the page's EL can read it on any version of Jakarta EL */
	public record Stat(String name, long result) {
		public String getName() { return name; }
		public long getResult() { return result; }
	}

	/**
	 * Where stats.dat comes from. A new stream per bean: a bean is created for each request, so a stream
	 * shared by all of them would be at end-of-file for every bean after the first. Tests replace this.
	 */
	static Supplier<InputStream> source = () -> StatsBean.class.getResourceAsStream("/stats.dat");

	final List<StatsQuery> statsQueries = new ArrayList<>();

	@Inject
	EntityManager em;

	private List<Stat> cached;

	/** For CDI */
	public StatsBean() throws IOException {
		this(YearCalcs.getCurrentMembershipYear());
	}

	/** For tests: choose the "current" membership year instead of using the clock */
	StatsBean(int curYear) throws IOException {
		try (InputStream is = source.get()) {
			if (is == null) {
				throw new IOException("Could not get stats.dat");
			}
			var br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}
				var both = line.split("\\|");
				if (both.length != 2) {
					throw new IOException("Malformed line in stats.dat: " + line);
				}
				String query = subst(both[1], curYear).strip().replaceAll(";+$", "");
				statsQueries.add(new StatsQuery(subst(both[0], curYear).strip(), query));
			}
		}
	}

	private static String subst(String s, int curYear) {
		return s.replace("${CURYEAR}", Integer.toString(curYear))
			.replace("${LASTYEAR}", Integer.toString(curYear - 1));
	}

	List<StatsQuery> getQueries() {
		return statsQueries;
	}

	/** Used by the page's datatable; the page may ask more than once per request, so ask the database once */
	@Transactional
	public List<Stat> getStatistics() {
		if (cached == null) {
			List<Stat> results = new ArrayList<>();
			for (StatsQuery stat : statsQueries) {
				Long qResult = em.createNativeQuery(stat.query(), Long.class).getSingleResult() instanceof Long l ? l : null;
				if (qResult == null) {
					System.out.println("Warning: " + stat.query() + " returned null; it should be a COUNT query");
					continue;
				}
				results.add(new Stat(stat.name(), qResult));
			}
			cached = results;
		}
		return cached;
	}
}
