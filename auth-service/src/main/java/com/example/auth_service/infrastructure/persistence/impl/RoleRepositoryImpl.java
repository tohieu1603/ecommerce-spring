package com.example.auth_service.infrastructure.persistence.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.infrastructure.persistence.jpa.entities.PermissionJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.entities.RoleJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.PermissionJpaRepository;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.RoleJpaRepository;
import com.example.auth_service.infrastructure.persistence.mapper.RoleMapper;

import lombok.RequiredArgsConstructor;

/**
 * Jpa adapter for {@link RoleRepository}
 * 
 * <p>Previously a TODO-stub returning input unchanged — now routes through
 * {@link RoleMapper} and correctly resolves permission references from the persistence
 * context so Hibernate updates the {@code role_permissions} join table cleanly.
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository repository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final RoleMapper mapper;

    @Override
    @Transactional

    /**
     * Persists or updates a {@link Role}. Resolves referenced permissions to managed
     * entities before saving — otherwise Hibernate would treat them as detached.
     */
    public Role save(Role role) {
        boolean isNew = !repository.existsById(role.getId().value());

        Set<String> permIds = role.getPermissions().stream()
                .map(PermissionId::value)
                .collect(Collectors.toSet());

        Set<PermissionJpaEntity> permissionJpaEntitys = permIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(permissionJpaRepository.findByIdIn(permIds));
        
        RoleJpaEntity saved = repository.save(mapper.toEntity(role, permissionJpaEntitys, isNew));

        return mapper.toDomain(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(RoleId roleId) {
        return repository.findById(roleId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(RoleName roleName) {
        return repository.findByName(roleName.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByIdIn(Set<RoleId> roleIds) {
        Set<String> roles = roleIds.stream()
                .map(RoleId::value)
                .collect(Collectors.toSet());

        return repository.findByIdIn(roles).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByIdWithPermissions(RoleId roleId) {
        return repository.findByIdWithPermissions(roleId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByNameWithPermissions(RoleName roleName) {
        return repository.findByNameWithPermissions(roleName.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(RoleName roleName) {
        return repository.existsByName(roleName.value());
    }

    @Override
    @Transactional
    public void delete(Role role) {
        if(role.getId() != null) {
            repository.deleteById(role.getId().value());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    private Role toDomain(RoleJpaEntity entity) {

        Set<PermissionId> permissions = entity.getPermissions().stream()
                .map(p -> PermissionId.of(p.getId()))
                .collect(Collectors.toSet());

        return Role.reconstitute(
                RoleId.of(entity.getId()),
                RoleName.of(entity.getName()),
                entity.getDescription(),
                permissions,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

    }
}
