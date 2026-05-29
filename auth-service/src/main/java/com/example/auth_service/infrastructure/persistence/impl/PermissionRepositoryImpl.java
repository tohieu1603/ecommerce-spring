package com.example.auth_service.infrastructure.persistence.impl;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.permission.vo.PermissionName;
import com.example.auth_service.domain.repositories.PermissionRepository;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.PermissionJpaRepository;
import com.example.auth_service.infrastructure.persistence.mapper.PermissionMapper;

import lombok.RequiredArgsConstructor;

/**
 * Spring Data Jpa Adapter for {@link PermissionRepository}
 * 
 * <p>Previous version had a {@code save()} that returned its input unchanged - a silent
 * data -loss
 */

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {
    private final PermissionJpaRepository repository;
    private final PermissionMapper mapper;

    @Override
    @Transactional
    public Permission save(Permission permission) {
        return permission;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findById(PermissionId permissionId) {
        return repository.findById(permissionId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByName(PermissionName permissionName) {
        return repository.findByName(permissionName.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByIdIn(Set<PermissionId> permissionIds) {
        Set<String> ids = permissionIds.stream()
                .map(PermissionId::value)
                .collect(Collectors.toSet());

        return repository.findByIdIn(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByResource(String resource) {
        return repository.findByResource(resource).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> findByAction(String action) {
        return repository.findByAction(action).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Permission> findByResourceAndAction(String resource, String action) {
        return repository.findByResourceAndAction(resource, action)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(PermissionName permissionName) {
        return repository.existsByName(permissionName.value());
    }

    @Override
    public void delete(Permission permission) {
        if(permission.getId() != null) {
            repository.deleteById(permission.getId().value());
        }
    }

    @Override
    public List<Permission> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
