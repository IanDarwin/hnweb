package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PwChangeToken {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
    String email;
    String token;
    LocalDateTime created;

	public PwChangeToken() {
		// Empty, needed for JPA
	}

    public PwChangeToken(String email, String token) {
        this.email = email;
        this.token = token;
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
