package model;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class PwChangeToken {
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id int id;
    String email;
    String token;
    LocalDate created = LocalDate.now();

    public PwChangeToken(String token, String email) {
        this.token = token;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }
}
