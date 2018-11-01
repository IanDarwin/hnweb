package data;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import model.Member;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("memberList") @Default
@SessionScoped
@Repository 
public interface MemberList extends Serializable, EntityRepository<Member, Long>  {

	public static final String FIND_ALL_QUERY = "select m from Member m order by m.lastName asc";

	@Query(value=FIND_ALL_QUERY)
	List<Member> findAll();

	@Query(value="select m from Member m where m.expiryYear >= 2019 order by m.lastName asc")
	List<Member> findCurrent();

	/** Basic for login */
	Member findByEmailAndPassword(String email, String passwordHash);

	/** Find one member by both names; used in JSF converter */
	Member findByFirstNameLastName(String string, String string2);
}
