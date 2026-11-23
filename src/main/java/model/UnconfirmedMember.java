package model;

import jakarta.persistence.*;
import jakarta.inject.Named;

@Entity
@Named
@DiscriminatorValue("Unconfirmed")
public class UnconfirmedMember extends MemberBase {
}
