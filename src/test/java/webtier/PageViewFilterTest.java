package webtier;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageViewFilterTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	// These tests only test the XML parsing and the PageInfo returned from it!

	@Test
	void goodParseLoginReqdRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginreqd-restriction.xml"));
		assertTrue(pi.loginRequired);
		assertNotNull(pi.restriction);
	}

	@Test
	void goodParseLoginNotReqdRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginNotreqd-restriction.xml"));
		assertFalse(pi.loginRequired);
		assertNotNull(pi.restriction);
	}

	@Test
	void goodParseLoginUnspecNoRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginunspec-norestriction.xml"));
		assertFalse(pi.loginRequired);
		assertNull(pi.restriction);
	}
}
