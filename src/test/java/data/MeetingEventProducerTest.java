package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.Event;

public class MeetingEventProducerTest {
	
	private MeetingEventProducer meetingEventProducer;

	@Before
	public void setUp() throws Exception {
		meetingEventProducer = new MeetingEventProducer();
	}

	@Test
	public void testGetRightNumberEvents() {
		final List<Event> events = meetingEventProducer.getEvents();
		// No meetings May Jun Jul Aug Dec:
		assertEquals("number of meetings", 12 - 5, events.size());
	}

	@Test
	public void testGetFirstMeetingInFuture() {
		final List<Event> events = meetingEventProducer.getEvents();
		Event e = events.get(events.size() - 1);
		LocalDate when = e.getDate();
		assertTrue("first meeting in future", when.isAfter(LocalDate.now()));
	}
}
