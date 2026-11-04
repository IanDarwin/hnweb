package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Test the Config properties code

public class ConfigTest {

    @BeforeEach
    public void setup() {
        // Empty?
    }

    @Test
    public void setGetStringTest() {
        Config.setProperty("soup", "french onion");
        assertEquals("french onion", Config.getProperty("soup"));
    }

    @Test
    public void setGetIntTest() {
        Config.setProperty("soup", "123");
        assertEquals(123, Config.getIntProperty("soup"));
    }

    @Test
    public void testOrgName() {
        Config.setProperty("orgName", "Nerds Anonymous");
        assertEquals(Config.orgName(), "Nerds Anonymous");
    }
}
