package data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.Event;
import org.hibernate.annotations.processing.Find;

/**
 * This is a basic DAO-like interface for scheduled club Events.
 * @author Ian Darwin
 */
@Named("eventList")
public class EventList implements Serializable {

	@PersistenceContext
	private EntityManager entityManager;

	public List<Event> findUpcoming(LocalDate date) {
		return entityManager.createQuery(
				"FROM Event e WHERE e.date >= :date order by e.date asc", Event.class)
				.setParameter("date", date)
				.getResultList();
	}
}
