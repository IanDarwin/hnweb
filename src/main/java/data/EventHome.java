package data;

import javax.ejb.*;
import javax.enterprise.context.ConversationScoped;
import javax.inject.*;

import entity.EntityHome;
import model.Event;

@Stateful @Named @ConversationScoped
public class EventHome extends EntityHome<Event,Long> {
	
	private static final long serialVersionUID = 1L;

	static final String LIST_PAGE = "EventList";
	
	public EventHome() {
		System.out.println("EventHome.EventHome()");
	}

	@Override
	public Event newInstance() {
		return new Event();
	}

	@Override
	public String getListPage() {
		return LIST_PAGE;
	}

	public void wire(String s) {
		wire(Long.parseLong(s));
	}
}
