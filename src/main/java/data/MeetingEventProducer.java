package data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.darwinsys.calendar.RecurringEventDatePicker;

import model.Event;

/**
 * Club-specific scheduler bean. Should replace with a DSL somedecade.
 * Regular meetings "are held on the 3rd Tuesday of each month from September to April, except for December."
 * @author Ian Darwin
 */
@Named @ApplicationScoped
public class MeetingEventProducer {

    private static final String REGULAR_MEETING = "Speaker's Night";
    private static final String FEBRUARY_MEETING = "Members' Night";

    private final static RecurringEventDatePicker MP = 
    		new RecurringEventDatePicker(3, DayOfWeek.TUESDAY, LocalTime.of(19, 00));

	@PersistenceContext EntityManager em;
	
	/** Persist the list of events created by getEvents() */
	@Transactional(value=TxType.REQUIRED)
    public String addEvents() {
		System.out.println("MeetingEventProducer.addEvents()");
		for (Event evt : getEvents()) {
			em.persist(evt);
		}
        return EventHome.LIST_PAGE + MemberHome.FORCE_REDIRECT;
	}

	/** Create the list of events for the next 12 months */
	protected List<Event> getEvents() {
		List<Event> list = new ArrayList<>();
		// Get all meetings for the next year
        for (int i = 0; i < 12; i++) {
			final LocalDate when = MP.getEventLocalDateTime(i).toLocalDate();
			// Regular meetings run from September to April.
			if (!isMeetingMonth(when)) {
				continue;
			}
			Event evt = new Event();
			evt.setDate(when);
			evt.setTimes("7-9 PM");
			evt.setTitle(when.getMonth() == Month.FEBRUARY ? FEBRUARY_MEETING : REGULAR_MEETING);
			evt.setSpeakerBio("T.B.A.");
			list.add(evt);
		}
		return list;
	}

    public boolean isMeetingMonth(final LocalDate when) {
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
}
