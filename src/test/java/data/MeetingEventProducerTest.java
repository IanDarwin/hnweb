package data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Event;

class MeetingEventProducerTest {
	
	private MeetingEventProducer meetingEventProducer;

	@BeforeEach
	void setUp() throws Exception {
		meetingEventProducer = new MeetingEventProducer();
	}

	@Test
	void getRightNumberEvents() {
		final List<Event> events = meetingEventProducer.getEvents();
		// No meetings May Jun Jul Aug Dec:
		assertEquals(12 - 5, events.size(), "number of meetings");
	}

	@Test
	void getFirstMeetingInFuture() {
		final List<Event> events = meetingEventProducer.getEvents();
		Event e = events.get(events.size() - 1);
		LocalDate when = e.getDate();
		assertTrue(when.isAfter(LocalDate.now()), "first meeting in future");
	}
}
