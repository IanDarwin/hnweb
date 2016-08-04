package data;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import entity.EntityHome;
import model.Event;

@Stateful @Named @ConversationScoped
public class EventHome extends EntityHome<Event,Long> {
	
	private static final long serialVersionUID = 1L;

	// This can't be EventList as non-priv users share the View page
	static final String LIST_PAGE = "index";
	
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
