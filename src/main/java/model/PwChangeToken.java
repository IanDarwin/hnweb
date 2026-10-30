package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PwChangeToken {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	long id;
    String email;
    String token;
    LocalDateTime created;

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

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
