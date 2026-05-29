package com.example.auth_service.domain.models.role;


import com.example.auth_service.domain.events.DomainEvent;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.role.event.PermissionGrantedEvent;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.role.vo.RoleName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.example.auth_service.domain.models.role.event.PermissionRevokedEvent;
import com.example.auth_service.domain.shared.AggregateRoot;

/**
 * Role aggregate root - a named bundle of permissions
 * 
 * <p>Permisison lists are never mutated externally; callers go through {@link #grantPermission}
 * and {@link #revokePermission} to change the permissions of a role. This allows us to emit domain events
 * when permissions are changed, which can be used to update read models or trigger other side effects
 */

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"id", "name", "description"})
public class Role extends AggregateRoot{

    @EqualsAndHashCode.Include
    private RoleId id;
    private RoleName name;
    private String description;
    private Set<PermissionId> permissions;
    private Instant createdAt;
    private Instant updatedAt;

    private Role() {
        this.permissions = new HashSet<>();
    }
    public static Role create(
            RoleName name,
            String description
    ) {
        Role role = new Role();
        role.id = RoleId.generate();
        role.name = name;
        role.description = description;
        role.createdAt = Instant.now();
        role.updatedAt = Instant.now();
        role.permissions = new HashSet<PermissionId>();

        return role;
    }
    public static Role reconstitute(
            RoleId id,
            RoleName name,
            String description,
            Set<PermissionId> permissions,
            Instant createdAt,
            Instant updatedAt
    ) {
        Role role = new Role();
        role.id = id;
        role.name = name;
        role.description = description;
        role.createdAt = createdAt;
        role.updatedAt = updatedAt;
        role.permissions = new HashSet<>(permissions);

        return role;
    }
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }
    public void grantPermissions(PermissionId permissions) {
        if(this.permissions.contains(permissions)) {
            return;
        }
        this.permissions.add(permissions);
        this.updatedAt = Instant.now();

        registerEvent(new PermissionGrantedEvent(id.value(), permissions.value()));
    }
    public void revokePermissions(PermissionId permissions) {
        if(!this.permissions.contains(permissions)) {
            return;
        }
        this.permissions.remove(permissions);
        this.updatedAt = Instant.now();

        registerEvent(new PermissionRevokedEvent(id.value(), permissions.value()));
        
    }
    public boolean hasPermissions(PermissionId permissionId) {
        return this.permissions.contains(permissionId);
    }
    public Set<PermissionId> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
