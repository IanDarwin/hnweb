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

import model.Member;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Apache DeltaSpike Data.
 * The methods in the inherited interface suffice for many apps!
 * @author Ian Darwin
 */
@Named("memberList") @Default
@SessionScoped
@Repository
public interface MemberList extends Serializable, CrudRepository<Member, Long> {

	String FIND_ALL_QUERY = "select m from Member m order by lower(m.lastName) asc";

	@Query(value=FIND_ALL_QUERY)
	Stream<Member> findAll();

	Member findBy(Long id);

	@Query(value="select m from Member m where m.memberType != 'Secondary' order by lower(m.lastName) asc")
	List<Member> findNonSecondaries();

	/** Basic for login */
	Member findByEmailAndPassword(String email, String passwordHash);

	/** Find one member by both names; used in JSF converter */
	Member findByFirstNameAndLastName(String string, String string2);
}
