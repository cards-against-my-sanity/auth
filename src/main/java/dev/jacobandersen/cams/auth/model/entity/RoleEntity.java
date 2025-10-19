package dev.jacobandersen.cams.auth.model.entity;

import dev.jacobandersen.cams.auth.model.domain.Role;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role toDomain() {
        return new Role(
                getId(),
                getName()
        );
    }
}
