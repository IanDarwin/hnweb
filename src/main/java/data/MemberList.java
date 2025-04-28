package data;

import java.util.List;
import java.util.stream.Stream;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import model.Member;

/**
 * This is a basic DAO-like interface for use by JSF or EJB.
 * Methods are implemented for us by Jakarta Data.
 * The methods in the inherited interface suffice for many apps!
 */
@Named("memberList")
@Repository
@SessionScoped
public interface MemberList {

	@Find @OrderBy(value="lastName", ignoreCase=true)
	Stream<Member> findAll();

	@Find
	Member findById(Long id);

	@Find @OrderBy(value="lastName", ignoreCase=true)
	@Query(value="select m from Member m JOIN FETCH m.roles JOIN FETCH m.masterMember JOIN FETCH m.secondaryMembers where m.memberType != 'Secondary'")
	List<Member> findNonSecondaries();

	/** Basic for login */
	@Find
	Member findByEmail(String email);

	@Find
	@Query("SELECT m FROM Member m LEFT JOIN FETCH m.roles LEFT JOIN FETCH m.masterMember LEFT JOIN FETCH m.secondaryMembers WHERE m.email = :email AND m.password = :password")
	Member findByEmailAndPassword(String email, String password);

	/** Find one member by firstname lastname; used in JSF converter */
	@Find
	Member findByFirstNameAndLastName(String firstName, String lastName);
}
