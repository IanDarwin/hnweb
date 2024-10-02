package model;

import jakarta.persistence.*;

@Entity
public class MemberRole {

    @Id
    long memberId;

    @Column(name="roles")
    String roleName;

    public MemberRole() {
        // Empty
    }

    public MemberRole(String roleName) {
        this.roleName = roleName;
    }

    public String getName() {
        return roleName;
    }
}
