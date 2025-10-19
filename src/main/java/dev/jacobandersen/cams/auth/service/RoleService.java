package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.model.domain.Role;
import dev.jacobandersen.cams.auth.model.entity.RoleEntity;
import dev.jacobandersen.cams.auth.repo.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll().stream().map(RoleEntity::toDomain).toList();
    }
}
