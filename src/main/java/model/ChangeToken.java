package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="pwchangetoken") // Previous entity name
public class ChangeToken {
	public static final String PURPOSE_PASSWORD = "PASSWORD";
	public static final String PURPOSE_SIGNUP = "SIGNUP";

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
    String email;
    String token;
    LocalDateTime created;
	String purpose;     // null in older rows means PASSWORD


	public ChangeToken() {
		// Empty, needed for JPA
	}

    public ChangeToken(String email, String token) {
        this(email, token, PURPOSE_PASSWORD);
    }
	public ChangeToken(String email, String token, String purpose) {
		this.email = email;
		this.token = token;
		this.purpose = purpose;
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

	public String getPurpose() {
		return purpose;
	}
}
