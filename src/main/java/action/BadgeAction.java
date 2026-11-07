package action;

import jakarta.inject.Named;

import model.Member;

@Named
public class BadgeAction {
    boolean executive;

    public boolean isExecutive() {
        return executive;
    }

    public void setExecutive(boolean executive) {
        this.executive = executive;
    }

    public String add(Member member) {
        System.out.println("Adding member = " + member + " to Badge Queue");
        // Do some work here...!
        return "/";
    }
}
