package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="pwchangetoken") // Previous entity name
public class ChangeToken {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
    String email;
    String token;
    LocalDateTime created;

	public ChangeToken() {
		// Empty, needed for JPA
	}

    public ChangeToken(String email, String token) {
        this.email = email;
        this.token = token;
    }

	@PrePersist
	public void ensureTimeStamp() {
		if (created == null) {
			created = LocalDateTime.now();
		}
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
