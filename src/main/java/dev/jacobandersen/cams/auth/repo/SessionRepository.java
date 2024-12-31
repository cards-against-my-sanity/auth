package dev.jacobandersen.cams.auth.repo;

import dev.jacobandersen.cams.auth.model.Session;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SessionRepository extends ListCrudRepository<Session, UUID> {
}
