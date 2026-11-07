package action;

import jakarta.inject.Named;

import model.Member;
import webtier.WebConstants;

@Named
public class BadgeAction {
    boolean executive;
    Member member;

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public boolean isExecutive() {
        return executive;
    }

    public void setExecutive(boolean executive) {
        this.executive = executive;
    }

    public String add(Member member) {
        System.out.println("Adding member = " + member + " to Badge Queue");
        // Do some work here...!
        return "/" + WebConstants.FORCE_REDIRECT;
    }
}
