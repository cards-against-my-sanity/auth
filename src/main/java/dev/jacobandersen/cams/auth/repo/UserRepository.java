package dev.jacobandersen.cams.auth.repo;

import dev.jacobandersen.cams.auth.model.User;
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
