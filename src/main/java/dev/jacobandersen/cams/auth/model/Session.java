package dev.jacobandersen.cams.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    @JsonIgnore
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @NotNull
    @Column(name = "expiration", nullable = false)
    private Timestamp expiration;

    public Session() {
    }

    public Session(User user) {
        this(user, Duration.ofDays(3));
    }

    public Session(User user, Duration ttl) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.expiration = new Timestamp(System.currentTimeMillis() + ttl.toMillis());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Timestamp getExpiration() {
        return expiration;
    }

    public void setExpiration(Timestamp expiration) {
        this.expiration = expiration;
    }

    @JsonIgnore
    public boolean isExpired() {
        if (expiration == null) return false;
        return expiration.before(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", expiration=" + expiration +
                '}';
    }
}
