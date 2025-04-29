package data;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import action.YearCalcs;
import org.junit.Ignore;
import org.junit.Test;

public class MemberHomeTest extends AbstractPersistenceTest {

	@Test
	public void testGetNewMemberAugustExpiryYear() {
		LocalDate aug31 = LocalDate.of(2011, 8, 29);
		assertEquals(2011, YearCalcs.getNewMemberExpiryYear(aug31));
	}
	
	@Test @Ignore
	public void testGetNewMemberSeptemberExpiryYear() {
		LocalDate sep1 = LocalDate.of(2011, 9, 01);
		assertEquals(2012, YearCalcs.getNewMemberExpiryYear(sep1));
	}

}
