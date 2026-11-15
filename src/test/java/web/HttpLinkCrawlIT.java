package web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import model.Config;

/**
 * Browser-less link checker, run against an ALREADY DEPLOYED copy of the app (not started by
 * "mvn test"). Logs in with a plain form POST, then follows every same-site link from the home
 * page, as an anonymous visitor and as an admin. Reports every page that gives an error status or
 * shows a server error, plus links that lead outside the app's context path, each with the page
 * that linked to it. Use after reorganizing pages.
 * <p>
 * Blind spots: no JavaScript runs, so anything loaded by AJAX (e.g. the MemberList table, whose
 * links appear only after it loads) is not seen; command buttons and commandLinks are not followed.
 * Visits one example of each page + set of query-parameter names, so MemberView.web?memberId=1 and
 * =2 count as one page.
 * <p>
 * Settings, from config.properties via {@link Config#getProperty(String)}, or -Dit.NAME=value:
 * <b>base</b> (URL of the deployed app, no trailing slash, incl. context path if any),
 * <b>adminemail</b>, <b>adminpw</b>. Because the committed src/test/resources/config.properties
 * shadows the live src/main/resources/config.properties on the test classpath, we also look in
 * the live copy, so secrets never need to be committed.
 */
class HttpLinkCrawlIT {

	/** Links we never follow: they change data or end the session. */
	static final Pattern SKIP = Pattern.compile("(?i)logout|logoff|delete|remove|signoff");

	/** Not HTML pages: status-checked only, not parsed for links. */
	static final Pattern DOWNLOAD = Pattern.compile("(?i)(CSV[A-Za-z]*\\.web|\\.(pdf|csv|zip|png|jpg|jpeg|gif|ico|css|js))(\\?.*)?$");

	/** Signs that the server (or JSF) is showing an error page with a 200 status */
	static final Pattern ERROR_TEXT = Pattern.compile(
		"HTTP Status \\d{3}|java\\.lang\\.\\w+(Exception|Error)|jakarta\\.faces\\.[A-Za-z.]*Exception|Caused by:|ViewExpiredException|WFLYUT\\d+");

	static final int MAX_PAGES = 400;

	/** Where to start; add entry points that aren't linked from the home page. */
	static final List<String> SEEDS = List.of("/", "/admin/MemberList.web", "/admin/EventList.web");

	@Test
	void anonymousLinksAllWork() throws Exception {
		crawl("anonymous", newClient());
	}

	@Test
	void adminLinksAllWork() throws Exception {
		var client = newClient();
		login(client, setting("adminemail"), setting("adminpw"));
		crawl("admin", client);
	}

	// ---------- the crawl ----------

	private void crawl(String who, Client client) throws Exception {
		Map<String, String> foundOn = new LinkedHashMap<>();	// url -> page it was linked from
		var queue = new ArrayDeque<String>();
		var seenKeys = new HashSet<String>();
		var problems = new ArrayList<String>();
		int visited = 0;

		for (String seed : SEEDS) {
			queue.add(url(seed));
			foundOn.put(url(seed), "(seed)");
		}

		while (!queue.isEmpty() && visited < MAX_PAGES) {
			String url = queue.poll();
			if (SKIP.matcher(url).find() || !seenKeys.add(key(url))) {
				continue;
			}
			visited++;
			String from = foundOn.get(url);

			HttpResponse<String> r;
			try {
				r = client.get(url);
			} catch (Exception e) {
				problems.add("ERR  " + url + "   <- linked from " + from + "  (" + e + ")");
				continue;
			}
			if (r.statusCode() >= 400) {
				problems.add(r.statusCode() + "  " + url + "   <- linked from " + from);
				continue;
			}
			if (DOWNLOAD.matcher(url).find()) {
				continue;
			}
			String body = r.body();
			var m = ERROR_TEXT.matcher(body);
			if (m.find()) {
				problems.add("ERROR TEXT '" + m.group() + "' on " + url + "   <- linked from " + from);
				continue;
			}

			String here = r.uri().toString();		// after any redirects
			Document doc = Jsoup.parse(body, here);
			for (var a : doc.select("a[href]")) {
				String href = a.absUrl("href");
				if (href.isEmpty() || href.startsWith("mailto:") || href.startsWith("javascript:")
						|| href.startsWith("tel:")) {
					continue;
				}
				int hash = href.indexOf('#');
				if (hash >= 0) href = href.substring(0, hash);
				if (href.isEmpty() || foundOn.containsKey(href)) {
					continue;
				}
				if (sameSite(href)) {
					foundOn.put(href, here);
					queue.add(href);
				} else if (sameHost(href)) {
					// Our host, but outside the app's context path: typical stale or hardcoded link
					problems.add("OUTSIDE APP  " + href + "   <- linked from " + here
						+ "  (link '" + a.attr("href") + "' ignores the context path?)");
					foundOn.put(href, here);
				}
			}
		}

		System.out.println("HttpLinkCrawlIT(" + who + "): visited " + visited + " pages, " + problems.size() + " problem(s)");
		assertTrue(problems.isEmpty(), who + " crawl found problems:\n" + String.join("\n", problems));
	}

	// ---------- HTTP and login ----------

	/** HttpClient with its own cookie jar = its own session */
	static class Client {
		final HttpClient http;
		Client(HttpClient http) { this.http = http; }

		HttpResponse<String> get(String url) throws IOException, InterruptedException {
			return http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		}

		HttpResponse<String> postForm(String url, Map<String, String> fields) throws IOException, InterruptedException {
			String body = fields.entrySet().stream()
				.map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
					+ URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));
			return http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
				HttpResponse.BodyHandlers.ofString());
		}
	}

	private static Client newClient() {
		var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		return new Client(HttpClient.newBuilder()
			.cookieHandler(cookies)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(15))
			.build());
	}

	/**
	 * Log in through the real Login page: fetch it, copy every field of the JSF form (including the
	 * hidden view state), fill in email and password, and post it as the Log in button would.
	 */
	private static void login(Client client, String email, String password) throws Exception {
		var page = client.get(url("/Login.web"));
		Document doc = Jsoup.parse(page.body(), page.uri().toString());
		var form = doc.selectFirst("form[id=loginForm]");
		if (form == null) {
			throw new AssertionError("No loginForm on " + page.uri() + " (status " + page.statusCode() + ")");
		}
		Map<String, String> fields = new LinkedHashMap<>();
		for (var input : form.select("input[name]")) {
			String type = input.attr("type");
			if (type.equals("submit") || type.equals("button")) continue;
			fields.put(input.attr("name"), input.val());
		}
		fields.put("loginForm:email", email);
		fields.put("loginForm:password", password);
		fields.put("loginForm:login", "Log in");		// the button's name=value, as a browser sends it

		var result = client.postForm(form.absUrl("action"), fields);
		Document after = Jsoup.parse(result.body(), result.uri().toString());
		if (result.statusCode() >= 400 || after.selectFirst("form[id=loginForm]") != null) {
			throw new AssertionError("Login as " + email + " failed (status " + result.statusCode()
				+ ", at " + result.uri() + "); is adminemail/adminpw right for " + base() + "?");
		}
	}

	// ---------- url helpers ----------

	private static boolean sameSite(String href) {
		return href.equals(base()) || href.startsWith(base() + "/");
	}

	private static boolean sameHost(String href) {
		try {
			return URI.create(href).getAuthority() != null
				&& URI.create(href).getAuthority().equalsIgnoreCase(URI.create(base()).getAuthority());
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/** page + the NAMES of its query parameters, so ?memberId=1 and ?memberId=2 count as one page */
	private static String key(String url) {
		URI u = URI.create(url);
		Set<String> names = new TreeSet<>();
		if (u.getRawQuery() != null) {
			for (String kv : u.getRawQuery().split("&")) {
				names.add(kv.split("=", 2)[0]);
			}
		}
		return u.getPath() + "?" + names;
	}

	// ---------- settings ----------

	private static String base() {
		String b = setting("base");
		return b.endsWith("/") ? b.substring(0, b.length() - 1) : b;
	}

	private static String url(String path) {
		return base() + (path.startsWith("/") ? path : "/" + path);
	}

	private static String setting(String key) {
		String v = System.getProperty("it." + key);
		if (v == null) v = Config.getProperty(key);
		// if (v == null) v = fromLiveConfig(key);
		if (v == null) {
			throw new IllegalStateException("Setting '" + key + "' not found: add it to "
				+ "src/main/resources/config.properties (see config-sample.properties) or pass -Dit." + key + "=...");
		}
		return v.trim();
	}

	private static String fromLiveConfig(String key) {
		Path live = Path.of("src/main/resources/config.properties");	// cwd is the project dir under Maven
		if (!Files.isReadable(live)) return null;
		var p = new Properties();
		try (var is = new FileInputStream(live.toFile())) {
			p.load(is);
		} catch (IOException e) {
			return null;
		}
		return p.getProperty(key);
	}
}
