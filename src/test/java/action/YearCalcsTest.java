package action;

import java.time.LocalDate;

import model.Member;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Membership year is from Sept 1 to Aug 31.
 */
public class YearCalcsTest {
			
	@ParameterizedTest(name = "Month {0}-{1} should give {2}")
	@CsvSource({
			"2019, 8, 2019",
			"2019, 9, 2020",
			"2020, 1, 2020",
			"2020, 3, 2020",
			"2050, 1, 2050",
	})
	public void testGetCurrentMembershipYear(int year, int month, int expectedYear) {
		LocalDate now = LocalDate.of(year, month, 12);
		assertEquals(expectedYear, YearCalcs.getCurrentMembershipYear(now));
	}

	@ParameterizedTest(name = "String {0} should give {1}")
	@CsvSource({
			"2023, 1, 2023",
			"2022, 1, 2022",
			"2021, 1, 2021",
			"2020, 1, 2020",
			"2019, 1, 2019",
			"2018, 1, 2018",
	})
	public void testGetNewMemberExpiryYear(int year, int month, int expectedYear) {
        LocalDate now = LocalDate.of(year, month, 1);
		assertEquals(expectedYear, YearCalcs.getCurrentMembershipYear(now));
	}

	@ParameterizedTest(name = "Member who expired {2}, as of {0}-{1}, should give {3}")
	@CsvSource({
			// now  | expYear | expect
			"2023, 9, 2019, true",
			"2023, 1, 2023, false",
			"2023, 6, 2023, false",
			"2023, 5, 2022, true",
	})
	public void testIsExpired(int nowYear, int nowMonth, int memberExpYear, boolean exp) {
		Member member = new Member();
		member.setExpiryYear(memberExpYear);
		// Day of Month doesn't matter, arbitrarily use 15:
		var now = LocalDate.of(nowYear, nowMonth, 15);
		assertEquals(YearCalcs.isExpired(member, now), exp);
	}

	/** A few single tests left over */
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
