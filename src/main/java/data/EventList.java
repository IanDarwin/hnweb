package data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import model.Event;

/**
 * This is a basic DAO-like interface for scheduled club Events.
 * @author Ian Darwin
 */
@Named("eventList")
public class EventList implements Serializable {

	@PersistenceContext
	private EntityManager entityManager;

	public List<Event> findAll() {
		return entityManager.createQuery("SELECT e FROM Event e").getResultList();
	}

	public List<Event> findUpcoming(LocalDate date) {
		return entityManager.createQuery(
				"SELECT e FROM Event e WHERE e.date >= :date order by e.date asc", Event.class)
				.setParameter("date", date)
				.getResultList();
	}
	public List<Event> findUpcoming() {
		return findUpcoming(LocalDate.now());
	}

	public void remove(Event evt) {
		entityManager.remove(entityManager.merge(evt));
	}
}
