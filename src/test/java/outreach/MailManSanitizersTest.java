package outreach;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MailManSanitizersTest {

    @Test
    public void testSanitizeToUtf8() {
        assertEquals("\"Hello\"--said I.", MailMan.sanitizeToUtf8("“Hello”—said I."));
    }
}
