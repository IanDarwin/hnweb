package data;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import model.MemberArchived;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

/**
 * List of deleted members
 * @author Ian Darwin
 */
@Named("deletedList") @Default
@SessionScoped
@Repository 
public interface DeletedList extends Serializable, EntityRepository<MemberArchived, Long>  {

	public static final String FIND_ALL_QUERY = "select m from MemberArchived m order by lower(m.lastName) asc";

	@Query(value=FIND_ALL_QUERY)
	List<MemberArchived> findAll();

	/** Find one member by both names; used in JSF converter */
	MemberArchived findByFirstNameAndLastName(String string, String string2);
}
