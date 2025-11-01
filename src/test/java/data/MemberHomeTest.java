package data;

import java.time.LocalDate;

import action.YearCalcs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberHomeTest extends AbstractPersistenceTest {

	@Test
	void getNewMemberAugustExpiryYear() {
		LocalDate aug31 = LocalDate.of(2011, 8, 29);
		assertEquals(2011, YearCalcs.getNewMemberExpiryYear(aug31));
	}

	@Test
	@Disabled
	void getNewMemberSeptemberExpiryYear() {
		LocalDate sep1 = LocalDate.of(2011, 9, 01);
		assertEquals(2012, YearCalcs.getNewMemberExpiryYear(sep1));
	}

}
