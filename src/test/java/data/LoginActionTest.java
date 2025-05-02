package data;

import action.LoginAction;
import jakarta.persistence.EntityManager;
import model.Member;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A failsome test of login action. Tries to use the actual MemberDAO
 * class but have to mock up too much of Hibernate for this to work.
 * This is a dead-end development, but left here in case a way's found to salvage it.
 */
public class LoginActionTest extends AbstractPersistenceTest {

    private static LoginAction target;

    // @BeforeAll
    public static void setUp() {
        target = new LoginAction();
        target.setPrinter(s -> System.out.println(s));
    }

    // @BeforeEach
    void setupLoginAction() {
        Member m = createAndSaveMember();
        saveMember(entityManager, m);
        final StatelessSession mockSession = mock(StatelessSession.class);
        final SessionFactory mockSessFactory = mock(SessionFactory.class);
        when(mockSession.getFactory()).thenReturn(mockSessFactory);
        final HibernateCriteriaBuilder mockCriteriaBuilder = mock(HibernateCriteriaBuilder.class);
        when(mockSessFactory.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
        target.setMemberDAO(new MemberDAO(entityManager));
    }

    // @Test
    public void loginTestGood() {
        target.setEmail("lottie@lotsa.stuff");
        target.setPassword("fiddle faddle");
        String ret = target.login();
        assertTrue(ret.startsWith("/index.web"));
        System.out.println("WE APPARENTLY LOGGED IN!");
    }

    // @Test
    public void loginTestBad() {
        target.setEmail("pierre@snubba.dub");
        target.setPassword("fuddle duddle");
        String ret = target.login();
        assertTrue(!ret.startsWith("/index.web"));
        System.out.println("WE DIDN'T LOG IN!");
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
