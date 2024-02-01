package data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import com.darwinsys.calendar.RecurringEventDatePicker;

import model.Event;
import webtier.WebConstants;

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
    		new RecurringEventDatePicker(RecurringEventDatePicker.LAST, DayOfWeek.TUESDAY, LocalTime.of(19, 00));

	@PersistenceContext EntityManager em;
	
	/** Persist the list of events created by getEvents() */
	@Transactional(value=TxType.REQUIRED)
    public String addEvents() {
		System.out.println("MeetingEventProducer.addEvents()");
		for (Event evt : getEvents()) {
			em.persist(evt);
		}
        return EventHome.LIST_PAGE + WebConstants.FORCE_REDIRECT;
	}

	/** Create the list of events for the next 12 months */
	protected List<Event> getEvents() {
		List<Event> list = new ArrayList<>();
		// Get all meetings for the next year
        for (int i = 0; i < 12; i++) {
			final LocalDate mtgDate = MP.getEventLocalDateTime(i).toLocalDate();
			// Regular meetings run from September to April.
			if (!isMeetingMonth(mtgDate)) {
				continue;
			}
			Event evt = new Event();
			evt.setDate(mtgDate);
			evt.setStartTime("1900");
			evt.setEndTime("2100");
			if (mtgDate.getMonth() != Month.FEBRUARY) {
				evt.setTitle(REGULAR_MEETING);
				evt.setSpeakerName("T.B.A.");
			} else {
				evt.setTitle(FEBRUARY_MEETING);
				evt.setSpeakerName("Various Members");
			}
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
