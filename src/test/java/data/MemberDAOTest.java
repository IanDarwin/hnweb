package data;

import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Member;
import model.MemberTransaction;
import model.PaymentType;
import model.TransactionReason;

import java.util.List;

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
        tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.delete(member);
        tx.commit();
        assertEquals(0, getEntityManager().createQuery("SELECT m FROM Member m", Member.class).getResultList().size());
    }

	@Test
	void testWithDetachedMemberTransaction() {
        System.out.println(1);
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.create(member);
		// Create detached MemberTransaction, like one of the scripts was doing.
        final MemberTransaction entity = new MemberTransaction(member, 2, TransactionReason.R, PaymentType.CASH);
        getEntityManager().persist(entity);
        member.getMemberTransactions().add(entity);
        tx.commit();
        tx = getEntityManager().getTransaction();
        final List<Member> members =
                getEntityManager().createQuery("SELECT m FROM Member m", Member.class).getResultList();
        members.forEach(System.out::println);
        assertEquals(1, members.size());
    }
		
}
