package outreach;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MailManSanitizersTest {

	final static String SHORT_INPUT = "“Hello”—said I.";

	// XXX This should have the em-dash backslash-u char, and so on
	final static String INPUT =
		"\u201cDon\u2018t cry for me, Argentina - \u2018Evita'";

	final static String EXPECT_UTF8 =
		"\"Don't cry for me, Argentina - 'Evita'";

	final static String EXPECT_HTML =
		"&quot;Don&apos;t cry for me, Argentina - &apos;Evita&apos;";

	@AfterAll
	public static void dump() {
		System.out.println("Input:\t" + INPUT);
		System.out.println("Expect:\t" + EXPECT_UTF8);
		System.out.println("Expect:\t" + EXPECT_HTML);
	}

    @Test
    public void testSanitizeToUtf8() {
        assertEquals(EXPECT_UTF8, MailMan.sanitizeToUtf8(INPUT));
    }

    @Test
    public void testSanitizeToHTML() {
        assertEquals(EXPECT_HTML, MailMan.sanitizeToHtml(INPUT));
    }
}
