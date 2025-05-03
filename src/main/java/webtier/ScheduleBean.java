package webtier;

import java.time.LocalDateTime;

import data.EventDAO;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

import model.Event;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.ScheduleEvent;

/**
 * Maps the calendar database table into PrimeFaces' ScheduleModel.
 * At least it will, someday.
 */
@Named @RequestScoped
public class ScheduleBean {

	private ScheduleModel eventModel;
	private ScheduleEvent event = new DefaultScheduleEvent();
	@Inject EventDAO eventDAO;

	public ScheduleBean() {
		eventModel = new DefaultScheduleModel();

		for (Event evt : eventDAO.findUpcoming()) {
			DefaultScheduleEvent event = DefaultScheduleEvent.builder()
					.title("Monthly Meeting")
					.startDate(evt.getStartDateTime())
					.endDate(evt.getEndDateTime())
					.build();
			eventModel.addEvent(event);
		}
	}

	public ScheduleModel getModel() {
		return eventModel;
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void addEvent() {
		if (event.getId() == null)
		  eventModel.addEvent( event );
		else
		  eventModel.updateEvent(event);
		event = new DefaultScheduleEvent(); //reset dialog form
	}

	public void onEventSelect(SelectEvent<ScheduleEvent> selectEvent) {
		event = selectEvent.getObject();
	}
	
	public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
		event = DefaultScheduleEvent.builder().startDate(selectEvent.getObject()).endDate(selectEvent.getObject().plusHours(1)).build(); 
	}
}
