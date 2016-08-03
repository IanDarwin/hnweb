package data;

import javax.ejb.Stateful;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
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
}
