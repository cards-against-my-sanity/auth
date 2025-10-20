package dev.jacobandersen.cams.auth.model.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.jacobandersen.cams.auth.model.entity.RoleEntity;

import java.io.Serializable;
import java.util.UUID;

@JsonSerialize
public record Role(UUID id, String name) implements Serializable {
    public RoleEntity toEntity() {
        final RoleEntity entity = new RoleEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }
}
