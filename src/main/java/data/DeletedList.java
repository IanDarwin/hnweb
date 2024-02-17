package data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;

import model.MemberArchived;

/**
 * List of deleted members
 * @author Ian Darwin
 */
@Named("deletedList") @Default
@SessionScoped
@Repository
public interface DeletedList extends Serializable, CrudRepository<MemberArchived, Long> {

	public static final String FIND_ALL_QUERY = "select m from MemberArchived m order by lower(m.lastName) asc";

	@Query(value=FIND_ALL_QUERY)
	Stream<MemberArchived> findAll();

	/** Find one member by both names; used in JSF converter */
	MemberArchived findByFirstNameAndLastName(String string, String string2);
}
