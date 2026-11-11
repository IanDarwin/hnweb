package data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.Member;
import model.MemberArchived;
import model.MemberBase;
import model.MemberType;
import org.hibernate.annotations.processing.Find;

/**
 * List of deleted members
 * @author Ian Darwin
 */
@Named("deletedList") @Default
@SessionScoped
public class DeletedList implements Serializable {

	@PersistenceContext
	private EntityManager entityManager;

	public static final String FIND_ALL_QUERY = "select m from MemberArchived m order by lower(m.lastName) asc";

	public List<MemberArchived> findAll() {
		return entityManager.createQuery(FIND_ALL_QUERY,MemberArchived.class).getResultList();
	}

	/** Find one member by both names; used in JSF converter */
	public Optional<MemberArchived> findByFirstNameAndLastName(String firstName, String lastName) {
		return Optional.of(entityManager.createQuery(
						"SELECT m FROM Member m WHERE m.firstName = :firstName AND m.lastName = :lastName", MemberArchived.class)
				.setParameter("firstName", firstName)
				.setParameter("lastName", lastName)
				.getSingleResult());
	}

	/** Name of an archived secondary's master, whether or not the master is archived too */
	public String masterName(MemberArchived ma) {
		Long id = ma.getMasterMemberId();
		if (id == null) {
			return "(none)";
		}
		MemberBase master = entityManager.find(MemberBase.class, id);
		return master == null ? "(unknown)" : master.getName();
	}

	/** True if this is a secondary whose master is still archived: undelete the master instead */
	public boolean undeleteViaMaster(MemberArchived ma) {
		if (ma.getMemberType() != MemberType.Secondary || ma.getMasterMemberId() == null) {
			return false;
		}
		return entityManager.find(MemberBase.class, ma.getMasterMemberId()) instanceof MemberArchived;
	}
}
