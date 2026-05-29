package com.example.auth_service.domain.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.user.User;

/**
 * Cross-aggregate authorisation rules (permission/role checks) that span the
 * {@link User}, {@link Role}, and {@link Permission} aggregates
 * 
 * <p>Stateless, framework-free can run inside any layer or test harness
 */
public class AuthenticationDomainService {
        /** Does the user have a named permission through any of their roles? */
        public boolean hasPermission(User user, List<Role> roles, List<Permission> permissions, String permissionName) {
                Set<String> permissionId = exytractPermissionIds(roles);

                return permissions.stream()
                        .filter(p -> permissionId.contains(p.getId().value()))
                        .anyMatch(p -> p.getName().equals(permissionName));
        }

        public boolean hasRole(User user, List<Role> roles, String roleName) {
                return roles.stream()
                        .anyMatch(p -> p.getName().equals(roleName));
        }

        public Set<String> getPermission(User user, List<Role> roles, List<Permission> permissions) {
                Set<String> permissionId = exytractPermissionIds(roles);

                return permissions.stream()
                        .filter(p -> permissionId.contains(p.getId().value()))
                        .map(p -> p.getName().value())
                        .collect(Collectors.toSet());
        }
        
        public boolean canAccessResource(User user, List<Role> roles,
                                        List<Permission> permissions,
                                        String resource, String action) {
                Set<String> permissionId = exytractPermissionIds(roles);

                return permissions.stream()
                        .filter(p -> permissionId.contains(p.getId().value()))
                        .allMatch(p -> p.grants(resource, action));
        }

        private Set<String> exytractPermissionIds(Collection<Role> roles) {
                return roles.stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(PermissionId::value)
                        .collect(Collectors.toSet());
        }
}
