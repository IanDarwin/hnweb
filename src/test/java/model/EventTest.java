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
		assertEquals(subject.getStartTime(), LocalTime.of(12,34).toString());
		assertEquals(subject.getEndTime(), LocalTime.of(15,00).toString());
	}
}
