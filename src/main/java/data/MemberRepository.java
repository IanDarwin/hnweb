package data;

import jakarta.data.repository.BasicRepository;
import model.Member;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Member entities.
 */
public interface MemberRepository extends BasicRepository<Member, Long> {

    // Jakarta Data allows defining custom query methods by name convention
    // Find all people ordered by last name ascending
    List<Member> findAllOrderByLastNameAsc();

    // Find a single person by their email address
    Optional<Member> findByEmail(String email);

    // The implementation is generated for us.
}
