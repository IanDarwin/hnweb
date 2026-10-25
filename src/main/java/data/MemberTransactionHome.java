package data;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Named;

import entity.EntityHome;
import model.MemberTransaction;

@Stateful @Named @ConversationScoped
public class MemberTransactionHome extends EntityHome<MemberTransaction,Long> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public MemberTransaction newInstance() {
		return new MemberTransaction();
	}

	@Override
	public String getListPage() {
		return "MemberTransactionList";
	}

	/** Entry point for f:viewAction, which passes the request parameter as a String. */
	public void wire(String s) {
		if (s == null || s.isEmpty()) {
			instance = newInstance();
			return;
		}
		wire(Long.parseLong(s));
	}
}
