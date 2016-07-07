package data;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import model.Event;

@ManagedBean
public class EventList {

	@Inject MeetingEventProducer mp;
	@Inject DbEventProducer dp;
	
	public EventList() {
		System.out.println("EventList.EventList()");
	}

	public List<Event> getEvents() {
		List<Event> all = mp.getEvents();
		all.addAll(dp.getEvents());
		Collections.sort(all, (e1,e2)->e1.getDate().compareTo(e2.getDate()));
		return all;
	}
}
