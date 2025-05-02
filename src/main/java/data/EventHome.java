package data;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import entity.EntityHome;
import model.Event;

@Stateful @Named @ConversationScoped
public class EventHome extends EntityHome<Event,Long> {

	@Inject public EventDAO eventDAO;
	
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
		if (s == null || s.isEmpty()) {
			instance = newInstance();
			return;
		}
		wire(Long.parseLong(s));
	}

	@Transactional
	public String remove(Event evt) {
		System.out.println("EventHome: Remove " + evt);
		eventDAO.remove(evt);
		return "EventList";
	}
}
