package dev.jacobandersen.cams.auth.repo;

import dev.jacobandersen.cams.auth.model.entity.RoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends ListCrudRepository<RoleEntity, UUID> {
    RoleEntity findByName(String name);
}
