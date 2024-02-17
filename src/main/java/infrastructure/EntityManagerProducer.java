package infrastructure;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

/**
 * "DeltaSpike Data requires an EntityManager exposed via a CDI producer -
 * which is common practice in Java EE6 applications...
 * This allows the EntityManager to be injected over CDI instead of 
 * only being used with a @PersistenceContext annotation."
 * XXX Dunno if this is still needed with Jakarta Data.
 * @author Code from http://deltaspike.apache.org/documentation/data.html
 */
@Dependent
public class EntityManagerProducer {
	
	@PersistenceUnit
	private EntityManagerFactory emf;

	@Produces // you can also make this @RequestScoped
	public EntityManager create() {
		return emf.createEntityManager();
	}

	public void close(@Disposes EntityManager em) {
		if (em.isOpen()) {
			em.close();
		}
	}
}
