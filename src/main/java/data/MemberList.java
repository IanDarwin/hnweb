package data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Model;
import jakarta.inject.Named;

import model.Member;
import org.hibernate.annotations.processing.Find;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Jakarta Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("memberList")
@SessionScoped
@Repository
@Default
@Model
public interface MemberList extends Serializable, CrudRepository<Member, Long> {

	String FIND_ALL_QUERY = "select m from Member m order by lower(m.lastName) asc";

	@Query(value=FIND_ALL_QUERY)
	Stream<Member> findAll();

	@Find
	Member findBy(Long id);

	@Find @Query(value="select m from Member m where m.memberType != 'Secondary' order by lower(m.lastName) asc")
	List<Member> findNonSecondaries();

	/** Basic for login */
	@Find
	Member findByEmailAndPassword(String email, String password);

	/** Find one member by firstname lastname; used in JSF converter */
	@Find
	Member findByFirstNameAndLastName(String firstName, String lastName);
}
