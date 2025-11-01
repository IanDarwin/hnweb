package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberDAOTest extends AbstractPersistenceTest {

    static MemberDAO memberDAO;
    protected Member member;

	@BeforeEach
	void setup() {
        entityManager = emf.createEntityManager();
        memberDAO = new MemberDAO(entityManager);
        member = new Member("Ronald", "J", "Thumper", "user@host.com");
    }

	@Test
	void createAndFind() {
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.create(member);
        tx.commit();
        long id = member.getId();
        Member m2 = memberDAO.findById(id);
        assertEquals(member, m2);
    }

	@Test
	void createAndDelete() {
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.create(member);
        tx.commit();
        long id = member.getId();
        tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.delete(member);
        tx.commit();
        assertEquals(0, getEntityManager().createQuery("SELECT m FROM Member m").getResultList().size());
    }
}
