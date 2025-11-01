package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventTest {
	Event subject;

	@BeforeEach
	void bleah() {
		subject = new Event();		
	}

	@Test
	void times() {
		subject.setStartTime("12:34");
		subject.setEndTime("15:00");
		assertEquals(subject.getStartTime(), LocalTime.of(12,34).toString());
		assertEquals(subject.getEndTime(), LocalTime.of(15,00).toString());
	}

	@Test
	void localDateTimes() {
		subject.setDate(LocalDate.of(2001, 9, 11));
		subject.setStartTime("12:34");
		subject.setEndTime("15:00");
		assertEquals(subject.getStartDateTime(), LocalDateTime.of(2001, 9, 11, 12,34));
		assertEquals(subject.getEndDateTime(), LocalDateTime.of(2001, 9, 11, 15,00));
	}
}
