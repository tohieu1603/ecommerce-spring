package com.example.auth_service.application.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.port.RolePermissionCachePort;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.repositories.PermissionRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class UserPermissionLookupService {
    private final RolePermissionCachePort rolePermissionCachePort;
    private final PermissionRepository permissionRepository;

    public Set<String> getPermissionsForRoles(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();

        return roles.stream()
                .flatMap(role -> permissionNamesFor(role).stream())
                .collect(Collectors.toSet());
    }

    private Set<String> permissionNamesFor(Role role) {
        String roleName = role.getName().value();

        Set<String> cached = rolePermissionCachePort.get(roleName);
        if(cached != null) return cached;

        Set<String> fromDb = role.getPermissions().isEmpty()
            ? Set.of()
            : permissionRepository.findByIdIn(role.getPermissions()).stream()
                    .map(p -> p.getName().value())
                    .collect(Collectors.toSet());
        rolePermissionCachePort.put(roleName, fromDb);

        return fromDb;

    }
}
