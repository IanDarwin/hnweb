package data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import model.Event;

/**
 * This is a basic DAO-like interface for scheduled club Events.
 * @author Ian Darwin
 */
@Named("eventList")
@RequestScoped
public class EventDAO extends GenericDAO<Event> implements Serializable {

	@PersistenceContext
	private EntityManager entityManager;

	EventDAO() {
		super(Event.class);
	}

	/** Find upcoming events from a given date forward
	 * @param date The date to start
	 * @return List of Events
	 */
	public List<Event> findUpcoming(LocalDate date) {
		return entityManager.createQuery(
				"SELECT e FROM Event e WHERE e.date >= :date order by e.date asc", Event.class)
				.setParameter("date", date)
				.getResultList();
	}

	/** Find upcoming events from today forward.
	 * @return List of Events
	 */
	public List<Event> findUpcoming() {
		return findUpcoming(LocalDate.now());
	}
}
