package action;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Membership year is from Sept 1 to Aug 31.
 */
public class YearCalcsTest {
			
	@ParameterizedTest(name = "Month {0}-{1} should give {2}")
	@CsvSource({
			"2019, 8, 2020",
			"2019, 9, 2020",
			"2020, 1, 2020",
			"2020, 3, 2021",
			"2050, 1, 2050",
	})
	public void testGetCurrentMembershipYear(int year, int month, int expectedYear) {
		LocalDate now = LocalDate.of(year, month, 1);
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
}
