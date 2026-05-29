package com.example.auth_service.application.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.auth_service.application.dtos.PermissionDTO;
import com.example.auth_service.application.dtos.RoleDTO;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.user.User;

@Component
public class UserDtoMapper {
    public UserDTO toDto(User user, Collection<Role> roles, Collection<Permission> permissions) {
        Set<String> roleNames = roles.stream()
            .map(role -> role.getName().value())
            .collect(Collectors.toSet());

        Set<String> permissionNames = permissions.stream()
            .map(permission -> permission.getName().value())
            .collect(Collectors.toSet());
        
            var s = user.getAccountStatus();
        return new UserDTO(
                user.getId().value(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getPersonName().firstName(),
                user.getPersonName().lastName(),
                s.enabled(),
                s.accountNonExpired(),
                s.accountNonLocked(),
                s.credentialsNonExpired(),
                roleNames,
                permissionNames,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                s.lastLogin()
        );
    }

    public UserDTO toDto(User user, Collection<Role> roles) {
        return toDto(user, roles, List.of());
    }

    public RoleDTO toDto(Role roles, Set<String> grantedPermissions) {
        return new RoleDTO(
                roles.getId().value(),
                roles.getName().value(),
                roles.getDescription(),
                grantedPermissions,
                roles.getCreatedAt(),
                roles.getUpdatedAt()
        );
    }
    public PermissionDTO toDto(Permission p) {
        return new PermissionDTO(
                p.getId().value(),
                p.getName().value(),
                p.getName().resource(),
                p.getName().action(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
