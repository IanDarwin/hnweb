package data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        System.out.println("createAndDelete: member id " + id);
        tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.delete(getEntityManager().merge(member));
        tx.commit();
        assertEquals(0, getEntityManager().createQuery("SELECT m FROM Member m").getResultList().size());
    }

    @Test
    void createAndDeleteComplex() {
        EntityTransaction tx = getEntityManager().getTransaction();
        tx.begin();
        memberDAO.create(member);
        member.getMemberTransactions().add(new MemberTransaction(member, 1, TransactionReason.N, PaymentType.CASH));
        member.setMemberType(MemberType.Family);
        Member spouse = new Member("Melly", "T", "Grump", "melly@grift.bar");
        spouse.setMasterMember(member);
        Set secondaries = new HashSet<Member>();
        secondaries.add(spouse);
        member.setSecondaryMembers(secondaries);
        tx.commit();
        long id = member.getId();
        tx = getEntityManager().getTransaction();
        tx.begin();
        Member member = memberDAO.findById(id);
        memberDAO.delete(member);
        tx.commit();
        assertEquals(0, getEntityManager().createQuery("SELECT m FROM Member m").getResultList().size());
    }
}
