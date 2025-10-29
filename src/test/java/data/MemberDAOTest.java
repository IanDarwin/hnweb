package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Member;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemberDAOTest extends AbstractPersistenceTest {

    static MemberDAO memberDAO;
    protected Member member;

    @BeforeEach
    public void setup() {
        entityManager = emf.createEntityManager();
        memberDAO = new MemberDAO(entityManager);
        member = new Member("Ronald", "J", "Thumper", "user@host.com");
    }

    @Test
    public void testCreateAndFind() {
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.create(member);
        tx.commit();
        long id = member.getId();
        Member m2 = memberDAO.findById(id);
        assertEquals(member, m2);
    }

    @Test
    public void testCreateAndDelete() {
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
