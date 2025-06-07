package dev.jacobandersen.cams.auth.repo;

import dev.jacobandersen.cams.auth.model.domain.Role;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends ListCrudRepository<Role, UUID> {
    Role findByName(String name);
}
