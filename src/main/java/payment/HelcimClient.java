package payment;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import model.Config;

/**
 * Talks to Helcim for HelcimPay.js card payments.
 * <p>
 * The flow (see https://devdocs.helcim.com/docs/overview-of-helcimpayjs):
 * <ol>
 * <li>{@link #initialize} - SERVER to server: we tell Helcim the amount, and get back a
 *     checkoutToken (public, goes to the browser) and a secretToken (stays on the server).
 * <li>The browser shows Helcim's payment iframe for that checkoutToken; the card details go
 *     straight to Helcim and never touch this server.
 * <li>The iframe reports the result to the page, which sends it to us; {@link #validate} checks
 *     Helcim's SHA-256 hash (which only someone knowing the secretToken can have produced), that
 *     the payment was approved, and that the amount and currency are what WE asked for.
 * </ol>
 * Never trust the browser's word that a payment happened; only {@link #validate} says so.
 * <p>
 * Settings in config.properties: <b>helcim.apitoken</b> (from a Helcim "API Access Configuration";
 * keep it out of git), optional <b>helcim.apiurl</b> (for testing).
 */
@ApplicationScoped
public class HelcimClient {

	private static final Logger logger = Logger.getLogger(HelcimClient.class.getName());

	static final String DEFAULT_INITIALIZE_URL = "https://api.helcim.com/v2/helcim-pay/initialize";

	/** Both tokens are good for 60 minutes (Helcim's rule), or until the transaction completes. */
	public record Checkout(String checkoutToken, String secretToken) implements Serializable {
		@Override
		public String toString() {
			return "Checkout[checkoutToken=" + checkoutToken + ", secretToken=****]";	// never log the secret
		}
	}

	/** What we keep of an approved, verified payment. */
	public record Payment(String transactionId, String approvalCode, BigDecimal amount,
			String currency, String cardNumber, String cardType) implements Serializable {
	}

	private final HttpClient http = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.build();

	public boolean isConfigured() {
		String t = Config.getProperty("helcim.apitoken");
		return t != null && !t.isBlank();
	}

	/** Start a checkout session for a card purchase of this amount. */
	public Checkout initialize(BigDecimal amount, String currency) throws PaymentException {
		String token = Config.getProperty("helcim.apitoken");
		if (token == null || token.isBlank()) {
			throw new PaymentException("helcim.apitoken is not configured");
		}
		String url = Config.getProperty("helcim.apiurl");
		if (url == null || url.isBlank()) {
			url = DEFAULT_INITIALIZE_URL;
		}
		String body = Json.createObjectBuilder()
			.add("paymentType", "purchase")
			.add("amount", amount)
			.add("currency", currency)
			.build().toString();

		HttpResponse<String> resp;
		try {
			resp = http.send(HttpRequest.newBuilder(URI.create(url.trim()))
				.timeout(Duration.ofSeconds(30))
				.header("accept", "application/json")
				.header("content-type", "application/json")
				.header("api-token", token.trim())
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build(), HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new PaymentException("Could not reach Helcim", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PaymentException("Interrupted while contacting Helcim", e);
		}
		if (resp.statusCode() != 200) {
			// The body can say why (bad token, bad currency...); it contains no secrets of ours
			logger.warning("Helcim initialize failed: HTTP " + resp.statusCode() + ": " + resp.body());
			throw new PaymentException("Helcim refused to start the payment (HTTP " + resp.statusCode() + ")");
		}
		try (var reader = Json.createReader(new StringReader(resp.body()))) {
			JsonObject o = reader.readObject();
			String checkoutToken = o.getString("checkoutToken", null);
			String secretToken = o.getString("secretToken", null);
			if (checkoutToken == null || secretToken == null) {
				throw new PaymentException("Helcim's reply had no checkoutToken/secretToken");
			}
			return new Checkout(checkoutToken, secretToken);
		} catch (JsonException | ClassCastException e) {
			throw new PaymentException("Could not understand Helcim's reply", e);
		}
	}

	/**
	 * Verify the browser's report of a payment.
	 *
	 * @param eventMessage The iframe's success message, as JSON text: {"data":{...},"hash":"..."}
	 * @param secretToken From {@link #initialize} for THIS checkout
	 * @param expectedAmount What we asked Helcim to charge
	 * @param expectedCurrency What we asked Helcim to charge in
	 * @return The verified payment
	 * @throws PaymentException if anything doesn't check out
	 */
	public static Payment validate(String eventMessage, String secretToken,
			BigDecimal expectedAmount, String expectedCurrency) throws PaymentException {
		if (eventMessage == null || eventMessage.isBlank() || secretToken == null) {
			throw new PaymentException("No payment result to validate");
		}
		JsonObject data;
		String hash;
		try (var reader = Json.createReader(new StringReader(eventMessage))) {
			JsonObject root = reader.readObject();
			data = root.getJsonObject("data");
			hash = root.getString("hash");
		} catch (JsonException | ClassCastException | NullPointerException e) {
			throw new PaymentException("Payment result is not in the expected format", e);
		}

		// Helcim's rule: SHA-256 (hex) of the compact JSON of data, followed by the secretToken.
		// Helcim hashes non-ASCII characters in their \\uXXXX form, so accept either spelling.
		String compact = data.toString();
		if (!hashMatches(compact, secretToken, hash) && !hashMatches(asciiOnly(compact), secretToken, hash)) {
			throw new PaymentException("Payment result failed hash validation");
		}

		// The hash proves Helcim said it; now check it says what WE need it to say.
		if (!"APPROVED".equalsIgnoreCase(data.getString("status", ""))) {
			throw new PaymentException("Payment was not approved (status " + data.getString("status", "?") + ")");
		}
		if (!"purchase".equalsIgnoreCase(data.getString("type", ""))) {
			throw new PaymentException("Unexpected transaction type " + data.getString("type", "?"));
		}
		BigDecimal paid = amountOf(data.get("amount"));
		if (paid == null || paid.compareTo(expectedAmount) != 0) {
			throw new PaymentException("Paid amount " + paid + " is not the expected " + expectedAmount);
		}
		if (!expectedCurrency.equalsIgnoreCase(data.getString("currency", ""))) {
			throw new PaymentException("Paid in " + data.getString("currency", "?") + ", expected " + expectedCurrency);
		}
		String txId = idOf(data.get("transactionId"));
		if (txId == null) {
			throw new PaymentException("Payment result has no transactionId");
		}
		return new Payment(txId, data.getString("approvalCode", null), paid, expectedCurrency.toUpperCase(),
			data.getString("cardNumber", null), data.getString("cardType", null));
	}

	static boolean hashMatches(String dataJson, String secretToken, String theirHash) {
		try {
			var md = MessageDigest.getInstance("SHA-256");
			byte[] mine = HexFormat.of().formatHex(
				md.digest((dataJson + secretToken).getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.US_ASCII);
			byte[] theirs = theirHash.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII);
			return MessageDigest.isEqual(mine, theirs);		// constant-time compare
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is required by the Java spec", e);
		}
	}

	/** Re-spell every non-ASCII char as \\uXXXX (lower-case hex), as Python's json.dumps and Helcim do. */
	static String asciiOnly(String json) {
		var sb = new StringBuilder(json.length());
		for (char c : json.toCharArray()) {
			if (c > 127) {
				sb.append(String.format("\\u%04x", (int) c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static BigDecimal amountOf(JsonValue v) {
		if (v instanceof JsonNumber n) {
			return n.bigDecimalValue();
		}
		if (v instanceof JsonString s) {
			try {
				return new BigDecimal(s.getString());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private static String idOf(JsonValue v) {
		if (v instanceof JsonNumber n) {
			return n.bigIntegerValue().toString();
		}
		if (v instanceof JsonString s && !s.getString().isBlank()) {
			return s.getString();
		}
		return null;
	}
}
