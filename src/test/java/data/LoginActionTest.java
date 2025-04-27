package data;

import action.LoginAction;
import jakarta.persistence.EntityManager;
import model.Member;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A simple test of login action. Uses the actual MemberList
 * but with the test database.
 */
public class LoginActionTest extends AbstractPersistenceTest {

    private static LoginAction target;

    @BeforeAll
    public static void setUp() {
        target = new LoginAction();
        target.setPrinter(s -> System.out.println(s));
    }

    @BeforeEach
    void setupLoginAction() {
        Member m = createAndSaveMember();
        saveMember(em, m);
        target.setMemberList(new MemberList_());
    }

    @Test
    public void loginTest() {
        target.setEmail("lottie@lotsa.stuff");
        target.setPassword("fiddle faddle");
        target.login();
        System.out.println("WE APPARENTLY LOGGED IN!");
    }

    private static void saveMember(EntityManager em, Member m) {
        em.getTransaction().begin();
        em.persist(m);
        em.getTransaction().commit();
    }

    private static Member createAndSaveMember() {
        Member tm = new Member();
        tm.setFirstName("Lotta");
        tm.setLastName("Lolz");
        tm.setEmail("lottie@lotsa.stuff");
        tm.setPassword("fiddle faddle");

        return tm;
    }
}
