package data;

import java.util.ArrayList;
import java.util.List;

import model.Event;

/*
@Named("memberList") @Default
@SessionScoped
@Repository 
 */
public interface DbEventProducer extends EventProducer/*, EntityRepository<Event, Long>*/ {

	//@Query(value="select evt from Event evt order by evt.date asc")
    public default List<Event> getEvents() {
    	// return findAll();
    	return new ArrayList<>();
	}
}
