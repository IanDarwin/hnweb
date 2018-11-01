package action;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Member;
import model.MemberTransaction;
import model.TransactionReason;

@Stateful @Named @RequestScoped
public class RenewAction implements Serializable {
	
	private static final long serialVersionUID = 832908490238178971L;
	
	Logger logger = Logger.getLogger(getClass().getName());

	private static final String VIEW_PAGE = "MemberView";
	private static final String FORCE_REDIRECT = "?faces-redirect=true";

	private Member member;
	private int numYears;
	
	@PersistenceContext EntityManager em;
	
	public RenewAction() {
		logger.info("RenewAction.RenewAction()");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String renew() {
		String message = "RenewAction.renew() for " + member + " for " + numYears + " years.";
		logger.info(message);
		if (numYears <= 0) {
			throw new IllegalArgumentException(message);
		}

		member = em.merge(member);	// make member managed
		MemberTransaction tx = new MemberTransaction(member, numYears, TransactionReason.R);
		em.persist(tx);
		member.getMemberTransactions().add(tx);
		member.acceptRenewal(tx);
		// XXX Needs more post-split refactoring
//		if (member.getMemberType() == MemberType.Family) {
//			for (Member m : member.getSecondaryMembers()) {
//				m.acceptRenewal(renewal);
//			}
//		}
		
		return VIEW_PAGE + FORCE_REDIRECT + "&memberId=" + member.getId();
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
		
