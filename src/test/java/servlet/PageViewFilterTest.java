package servlet;

import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

public class PageViewFilterTest {
	
	@Before
	public void setUp() throws Exception {
	}

	// These tests only test the XML parsing and the PageInfo returned from it!
	
	@Test
	public void testParseNoSuchFile() throws Exception {
		PageViewFilter.process("no such file");
		PageViewFilter.PageInfo pi = PageViewFilter.tl.get();
		assertFalse(pi.loginRequired);
		assertNull(pi.restriction);
	}
	@Test
	public void testGoodParseLoginReqdRestriction() throws Exception {
		PageViewFilter.process("src/test/resources/page-loginreqd-restriction.xml");
		PageViewFilter.PageInfo pi = PageViewFilter.tl.get();
		assertTrue(pi.loginRequired);
		assertNotNull(pi.restriction);
	}
	
	@Test
	public void testGoodParseLoginNotReqdRestriction() throws Exception {
		PageViewFilter.process("src/test/resources/page-loginNotreqd-restriction.xml");
		PageViewFilter.PageInfo pi = PageViewFilter.tl.get();
		assertFalse(pi.loginRequired);
		assertNotNull(pi.restriction);
	}
	
	@Test
	public void testGoodParseLoginUnspecNoRestriction() throws Exception {
		PageViewFilter.process("src/test/resources/page-loginunspec-norestriction.xml");
		PageViewFilter.PageInfo pi = PageViewFilter.tl.get();
		assertFalse(pi.loginRequired);
		assertNull(pi.restriction);
	}
}
