package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.dto.SignUpRequestDto;
import dev.jacobandersen.cams.auth.model.domain.Role;
import dev.jacobandersen.cams.auth.model.domain.User;
import dev.jacobandersen.cams.auth.model.entity.RoleEntity;
import dev.jacobandersen.cams.auth.model.entity.UserEntity;
import dev.jacobandersen.cams.auth.repo.RoleRepository;
import dev.jacobandersen.cams.auth.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleEntity userRoleEntity;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        userRoleEntity = roleRepository.findByName("user");
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean exists(String email, String nickname) {
        return userRepository.existsByEmailOrNickname(email, nickname);
    }

    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(id).map(UserEntity::toDomain);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(UserEntity::toDomain);
    }

    public User createUser(SignUpRequestDto dto) {
        final UserEntity entity = new UserEntity();
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setNickname(dto.getNickname());
        entity.setRoles(List.of(userRoleEntity));
        return userRepository.save(entity).toDomain();
    }

    public User setUserConfirmed(User user) {
        UserEntity entity = user.toEntity();
        entity.setConfirmed(true);
        entity = userRepository.save(entity);
        return entity.toDomain();
    }

    public User setUserRoles(User user, List<Role> roles) {
        UserEntity entity = user.toEntity();
        entity.setRoles(roles.stream().map(Role::toEntity).toList());
        entity = userRepository.save(entity);
        return entity.toDomain();
    }

    public User updateUserPassword(User user, String password) {
        UserEntity entity = user.toEntity();
        entity.setConfirmed(false);
        entity = userRepository.save(entity);
        return entity.toDomain();
    }

    public void deleteUser(User user) {
        if (null == user) return;
        userRepository.delete(user.toEntity());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public Optional<OidcUserInfo> loadOidcUserInfoByEmail(final String email) {
        return findUserByEmail(email).map(this::convertToOidcUserInfo);
    }

    public OidcUserInfo convertToOidcUserInfo(final User user) {
        return OidcUserInfo.builder()
                .subject(user.id().toString())
                .email(user.email())
                .emailVerified(user.confirmed())
                .preferredUsername(user.nickname())
                .claim("roles", user.roles().stream().map(Role::name).toList())
                .build();
    }
}
