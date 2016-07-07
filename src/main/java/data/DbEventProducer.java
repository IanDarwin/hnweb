package data;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import model.Event;


@Named("dbEventList") @Default
@SessionScoped
@Repository 
public abstract class DbEventProducer extends AbstractEntityRepository<Event, Long>
	implements EventProducer {

	public List<Event> getEvents() {
    	return findAll();
	}
}
