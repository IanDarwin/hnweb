package model;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Before;
import org.junit.Test;

public class EventTest {
	Event subject;
	
	@Before
	public void bleah() {
		subject = new Event();		
	}
	
	@Test
	public void testTimes() {
		subject.setStartTime("12:34");
		subject.setEndTime("15:00");
		var ret = subject.getTimes();
		assertEquals(ret[0], LocalTime.of(12,34));
		assertEquals(ret[1], LocalTime.of(15,00));
	}
}
