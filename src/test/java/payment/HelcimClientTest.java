package payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

/** Checks HelcimClient.validate, the part that decides whether we believe a payment happened. No network. */
class HelcimClientTest {

	static final String SECRET = "1d4b6437a8aabfe4b0ed93";
	static final BigDecimal FEE = new BigDecimal("45.00");

	/** Compact JSON, as Helcim hashes it */
	static String data(String status, String amount, String name) {
		return "{\"transactionId\":\"25764674\",\"status\":\"" + status + "\",\"type\":\"purchase\","
			+ "\"amount\":" + amount + ",\"currency\":\"CAD\",\"approvalCode\":\"T0E1S2\","
			+ "\"cardNumber\":\"5454****5454\",\"cardType\":\"MC\",\"cardHolderName\":\"" + name + "\"}";
	}

	static String sha256(String s) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
	}

	/** What the iframe sends: {"data":{...},"hash":"..."}, hash computed over hashedForm + secret */
	static String message(String dataJson, String hashedForm, String secret) throws Exception {
		return "{\"data\":" + dataJson + ",\"hash\":\"" + sha256(hashedForm + secret) + "\"}";
	}

	@Test
	void goodPaymentIsAccepted() throws Exception {
		String d = data("APPROVED", "\"45.00\"", "Pat Smith");
		var p = HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "CAD");
		assertEquals("25764674", p.transactionId());
		assertEquals("T0E1S2", p.approvalCode());
		assertEquals(0, FEE.compareTo(p.amount()));
	}

	@Test
	void numericAmountIsAcceptedToo() throws Exception {
		String d = data("APPROVED", "45", "Pat Smith");
		HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "CAD");
	}

	@Test
	void accentedNamesValidateWhetherHelcimHashedThemEscapedOrNot() throws Exception {
		String d = data("APPROVED", "\"45.00\"", "Renée");
		String escaped = data("APPROVED", "\"45.00\"", "Ren\\u00e9e");
		HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "CAD");
		HelcimClient.validate(message(d, escaped, SECRET), SECRET, FEE, "CAD");
	}

	@Test
	void wrongSecretIsRejected() throws Exception {
		String d = data("APPROVED", "\"45.00\"", "Pat Smith");
		assertThrows(PaymentException.class,
			() -> HelcimClient.validate(message(d, d, "some-other-secret"), SECRET, FEE, "CAD"));
	}

	@Test
	void tamperedDataIsRejected() throws Exception {
		String real = data("APPROVED", "\"45.00\"", "Pat Smith");
		String forged = data("APPROVED", "\"0.01\"", "Pat Smith");
		// Hash from the real data, but data swapped for a cheaper one
		String msg = "{\"data\":" + forged + ",\"hash\":\"" + sha256(real + SECRET) + "\"}";
		assertThrows(PaymentException.class, () -> HelcimClient.validate(msg, SECRET, FEE, "CAD"));
	}

	@Test
	void validlySignedButUnderpaymentIsRejected() throws Exception {
		String d = data("APPROVED", "\"1.00\"", "Pat Smith");
		assertThrows(PaymentException.class, () -> HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "CAD"));
	}

	@Test
	void declinedIsRejected() throws Exception {
		String d = data("DECLINED", "\"45.00\"", "Pat Smith");
		assertThrows(PaymentException.class, () -> HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "CAD"));
	}

	@Test
	void wrongCurrencyIsRejected() throws Exception {
		String d = data("APPROVED", "\"45.00\"", "Pat Smith");
		assertThrows(PaymentException.class, () -> HelcimClient.validate(message(d, d, SECRET), SECRET, FEE, "USD"));
	}

	@Test
	void garbageIsRejectedNotCrashed() {
		assertThrows(PaymentException.class, () -> HelcimClient.validate("not json", SECRET, FEE, "CAD"));
		assertThrows(PaymentException.class, () -> HelcimClient.validate("{}", SECRET, FEE, "CAD"));
		assertThrows(PaymentException.class, () -> HelcimClient.validate(null, SECRET, FEE, "CAD"));
		assertThrows(PaymentException.class, () -> HelcimClient.validate("{\"data\":1,\"hash\":\"x\"}", SECRET, FEE, "CAD"));
	}
}
