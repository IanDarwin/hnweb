package data;

import javax.ejb.*;
import javax.inject.*;

import entity.EntityHome;
import model.Event;

@Stateful @Named
public class EventHome extends EntityHome<Event,Long> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Event newInstance() {
		return new Event();
	}

	//@Override
	public String getListPage() {
		return "EventList";
	}
}
