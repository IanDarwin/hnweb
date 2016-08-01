package webtier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;

public class PageViewFilterTest {
	
	@Before
	public void setUp() throws Exception {
	}

	// These tests only test the XML parsing and the PageInfo returned from it!

	@Test
	public void testGoodParseLoginReqdRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginreqd-restriction.xml"));
		assertTrue(pi.loginRequired);
		assertNotNull(pi.restriction);
	}
	
	@Test
	public void testGoodParseLoginNotReqdRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginNotreqd-restriction.xml"));
		assertFalse(pi.loginRequired);
		assertNotNull(pi.restriction);
	}
	
	@Test
	public void testGoodParseLoginUnspecNoRestriction() throws Exception {
		PageFilter.PageInfo pi = PageFilter.parsePageFile(new FileInputStream(
				"src/test/resources/page-loginunspec-norestriction.xml"));
		assertFalse(pi.loginRequired);
		assertNull(pi.restriction);
	}
}
