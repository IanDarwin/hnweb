package data;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import action.YearCalcs;
import org.junit.Test;

public class MemberHomeTest extends AbstractPersistenceTest {

	@Test
	public void testGetNewMemberExpiryYear() {
		LocalDate aug31 = LocalDate.of(2011, 8, 29);
		assertEquals(2011, YearCalcs.getNewMemberExpiryYear(aug31));
		LocalDate sep1 = LocalDate.of(2011, 9, 01);
		assertEquals(2012, YearCalcs.getNewMemberExpiryYear(sep1));
	}

}
