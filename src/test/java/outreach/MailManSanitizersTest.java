package outreach;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MailManSanitizersTest {

    @Test
    public void testSanitizeToPlainText() {
        String htmlBody = "<span style='font-size: 12px; font-style: italic;'>Time</span><span style='font-size: 12px;'> is <span style='font-weight: bold;'>not</span> enough.</span>";
        final String actual = MailMan.sanitizeToPlainText(htmlBody);
        assertEquals("Sanitize to plain text", "Time is not enough.", actual);
    }

    @Test
    public void testSanitizeToUtf8() {
        assertEquals("\"Hello\"--said I.", MailMan.sanitizeToUtf8("“Hello”—said I."));
    }
}
