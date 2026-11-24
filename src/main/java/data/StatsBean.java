package data;

import java.io.*;
import java.util.*;
import jakarta.inject.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Named
public class StatsBean {

	record StatsQuery(String name, String query){}

	List<StatsQuery> statsQueries = new ArrayList<>();

	public record Stat(String name, long result){}

	static InputStream is;

	// "is" is created here normally; in test, we can overwrite it.
	static {
		is = StatsBean.class.getResourceAsStream("/stats.dat");
	}

	public StatsBean() throws IOException {

		if (is == null) {
			throw new RuntimeException("Could not get stats.dat");
		}
		String line;
		var br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			var both = line.split("\\|");
			if (both.length != 2) {
				throw new RuntimeException("Malformed line: " + line);
			}
			final StatsQuery e = new StatsQuery(both[0], both[1].replaceAll("\\$.CURYEAR.", "2026")); // XXX FIXME GENERALIZE
			System.out.println("e = " + e);
			statsQueries.add(e);
		}
	}

	public List<StatsQuery> getQueries() {
		return statsQueries;
	}

	@Inject
	EntityManager em;

	// Used by JSF Datatable
	@Transactional
	public List<Stat> getStatistics() throws Exception {
		List<Stat> results = new ArrayList<>();
		for (StatsQuery stat : statsQueries) {
			var q = em.createNativeQuery(stat.query, Long.class);
			Long qResult = (Long)q.getSingleResult();
			if (qResult == null) {
				System.out.println("Warning: " + stat.query + " returned null");
				continue;
			}
			results.add(new Stat(stat.name, qResult));
		}
		return results;
	}
}
