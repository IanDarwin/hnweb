package data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.darwinsys.calendar.RecurringEventDatePicker;

import model.Event;

/**
 * Regular meetings "are held on the 3rd Tuesday of each month from September to April, except for December."
 * @author Ian Darwin
 */
@Named @ApplicationScoped
public class MeetingEventProducer implements EventProducer {

    private static final String REGULAR_MEETING = "Regular meeting";

    private final static RecurringEventDatePicker MP = 
    		new RecurringEventDatePicker(3, DayOfWeek.TUESDAY, LocalTime.of(19, 00));
	
    public List<Event> getEvents() {
    	System.out.println("MeetingEventProducer.getEvents()");
    	List<Event> events = new ArrayList<>(10);
		// Get all meetings for the next year
        for (int i = 0; i < 12; i++) {
			final LocalDateTime when = MP.getEventLocalDateTime(i);
			// Regular meetings run from September to April.
			if (!isMeetingMonth(when)) {
				continue;
			}
			Event evt = new Event();
			evt.setDate(when);
			evt.setTitle(REGULAR_MEETING);
			events.add(evt);
		}
        System.out.println(events.size() + " meetings found");
        return events;
	}

    public boolean isMeetingMonth(final LocalDateTime when) {
    	switch(when.getMonth()) {
    	case MAY:
    	case JUNE:
    	case JULY:
    	case AUGUST:
    	case DECEMBER:
    		return false;
    	default:
    		return true;
    	}
    }

	public static RecurringEventDatePicker getMeetingDatePicker() {
		return MP;
	}
}
