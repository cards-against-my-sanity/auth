package dev.jacobandersen.cams.auth.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.jacobandersen.cams.auth.model.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@JsonSerialize
public record User(UUID id, String email, @JsonIgnore String password, String nickname, boolean confirmed, boolean banned, String banReason, List<Role> roles) implements Serializable, UserDetails {
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return !banned;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return confirmed;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).toList();
    }

    public UserEntity toEntity() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setEmail(email);
        userEntity.setPassword(password);
        userEntity.setNickname(nickname);
        userEntity.setConfirmed(confirmed);
        userEntity.setBanned(banned);
        userEntity.setBanReason(banReason);
        userEntity.setRoles(new ArrayList<>(roles.stream().map(Role::toEntity).toList()));
        return userEntity;
    }
}
