package data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;

import model.Event;
import org.hibernate.annotations.processing.Find;

/**
 * This is a basic DAO-like interface for scheduled club Events.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * @author Ian Darwin
 */
@Named("eventList") @Default
@Repository
public interface EventList {

	@Find
	@Query(value="FROM Event e WHERE e.date >= :date order by e.date asc")
	List<Event> findUpcoming(LocalDate date);
}
