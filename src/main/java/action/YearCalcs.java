package action;

import java.time.LocalDate;

import javax.inject.Named;

import model.Config;
import model.Member;
import model.MemberType;

/**
 * Membership year is from Sept 1 to Aug 31.
 */
@Named
public class YearCalcs {
			
	public static int getCurrentMembershipYear(LocalDate now) {
		int yearNow = now.getYear();
		int monthNow = now.getMonthValue();
		return (monthNow >= Config.RENEWAL_MONTH) ? yearNow + 1 : yearNow;
	}

	public static int getNewMemberExpiryYear(LocalDate now) {
		if (now.getMonthValue()>=Config.RENEWAL_MONTH)
			return getCurrentMembershipYear(now) + 1;
		return getCurrentMembershipYear(now);
	}

	public static boolean isExpired(Member m) {
		return isExpired(m, LocalDate.now());
	}
	/** Return true iff this member (or their Master Member) is expired */
	public static boolean isExpired(Member m, LocalDate now) {
		if (m.getMemberType() == MemberType.Secondary) {
			if (m.getMasterMember() == null) {
				System.err.printf("Member %s has no Master Member!\n", m);
				return true;	// flag it for attention!
			}
			return isExpired(m.getMasterMember(), now);
		}

		// If current year is past expiry year, obv. this member is expired.
		if (now.getYear() > m.getExpiryYear()) {
			return true;
		}
		// Not currently greater than exp year, so might be in it, must
		// check if we are currently past the expiry month.
		if (now.getYear() == m.getExpiryYear() &&
				now.getMonth().getValue() > Config.RENEWAL_MONTH)
			return true;
		// Otherwise we are not yet in the expiry year.
		return false;
	}

}
