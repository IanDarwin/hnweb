package model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Unconfirmed")
public class UnconfirmedMember extends MemberBase {
}
