package action;



import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import model.Member;
import model.MemberTransaction;
import model.TransactionReason;

@Named @RequestScoped
public class RenewAction implements Serializable {
	
	private static final long serialVersionUID = 832908490238178971L;

	private static final String VIEW_PAGE = "MemberView";
	private static final String FORCE_REDIRECT = "?faces-redirect=true";

	private Member member;
	private int numYears;
	
	@PersistenceContext EntityManager em;

	@Transactional(value=TxType.REQUIRED)
	public String renew() {
		String message = "RENEW ACTION: for " + member + " for " + numYears + " years.";
		if (numYears == 0) {
			throw new IllegalArgumentException(message);
		}
		System.out.println(message);
		MemberTransaction r = new MemberTransaction(member, numYears, TransactionReason.R);
		em.persist(r);
		member.acceptRenewal(r);
		
		return VIEW_PAGE  + "?memberId=" + member.getId() + FORCE_REDIRECT;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public int getNumYears() {
		return numYears;
	}

	public void setNumYears(int numYears) {
		this.numYears = numYears;
	}
}
		
