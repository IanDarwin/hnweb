package infrastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.ejb.Singleton;
import jakarta.inject.Named;

import com.darwinsys.geo.CanadaProvince;

import action.YearCalcs;
import model.MemberType;
import model.PaymentType;

@Singleton
@Named
public class Factories {

	public List<MemberType> getMemberTypes() {
		return Arrays.asList(MemberType.values());
	}

	public List<CanadaProvince> getCanadaProvinces() {
		return Arrays.asList(CanadaProvince.values());
	}

	/** The ways the club actually accepts payment at renewal time. */
	public List<PaymentType> getRenewalPaymentTypes() {
		return Arrays.asList(PaymentType.CASH, PaymentType.CHEQUE, PaymentType.ETRANSFER);
	}

	/**
	 * Years that make sense for a member's expiry: from last membership year
	 * through six years out. Computed, so it never needs a yearly edit.
	 */
	public List<Integer> getExpiryYears() {
		final int current = YearCalcs.getCurrentMembershipYear();
		List<Integer> years = new ArrayList<>();
		for (int y = current - 1; y <= current + 6; y++) {
			years.add(y);
		}
		return years;
	}
}
