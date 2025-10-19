package dev.jacobandersen.cams.auth.model.entity;

import dev.jacobandersen.cams.auth.model.domain.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", unique = true, nullable = false, length = 16)
    private String nickname;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "banned", nullable = false)
    private boolean banned;

    @Column(name = "ban_reason", length = 1024)
    private String banReason;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<RoleEntity> roleEntities;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public List<RoleEntity> getRoles() {
        return roleEntities;
    }

    public void setRoles(List<RoleEntity> roleEntities) {
        this.roleEntities = roleEntities;
    }

    public User toDomain() {
        return new User(
                getId(),
                getEmail(),
                getPassword(),
                getNickname(),
                isConfirmed(),
                isBanned(),
                getBanReason(),
                new ArrayList<>(getRoles().stream().map(RoleEntity::toDomain).toList())
        );
    }
}
