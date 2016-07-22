package model;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class EventTest {
	Event subject;
	
	@Before
	public void bleah() {
		subject = new Event();		
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testNewToOldDateConverters() {
		LocalDate d = LocalDate.of(2010, 11, 12);
		subject.setDate(d);
		Date od = subject.getLegacyDate();
		assertEquals(2010, 1900 + od.getYear());
		assertEquals(11, 1 + od.getMonth());
		assertEquals(12, od.getDate());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testOldToNewDateConverters() {
		Date d = new Date(2010-1900, 11-1, 12);
		subject.setLegacyDate(d);
		Date od = subject.getLegacyDate();
		assertEquals(2010, 1900 + od.getYear());
		assertEquals(11, 1 + od.getMonth());
		assertEquals(12, od.getDate());
		LocalDate ld = subject.getDate();
		assertEquals(2010, ld.getYear());
		assertEquals(11, ld.getMonth().getValue());
		assertEquals(12, ld.getDayOfMonth());
	}

}
