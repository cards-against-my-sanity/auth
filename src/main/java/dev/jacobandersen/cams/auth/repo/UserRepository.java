package dev.jacobandersen.cams.auth.repo;

import dev.jacobandersen.cams.auth.annotation.RFC5322Email;
import dev.jacobandersen.cams.auth.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends ListCrudRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailOrNickname(String email, String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
