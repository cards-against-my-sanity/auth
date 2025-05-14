package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.dto.in.SignUpRequestDto;
import dev.jacobandersen.cams.auth.model.Role;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.repo.RoleRepository;
import dev.jacobandersen.cams.auth.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Role userRole;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        userRole = roleRepository.findByName("user");
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
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(SignUpRequestDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRoles(List.of(userRole));
        return userRepository.save(user);
    }

    public void setUserConfirmed(User user) {
        user.setConfirmed(true);
        userRepository.save(user);
    }

    public void setUserRoles(User user, List<Role> roles) {
        user.setRoles(roles);
        userRepository.save(user);
    }

    public void updateUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
