package com.hieu.auth_service.domain.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hieu.auth_service.domain.models.permission.Permission;
import com.hieu.auth_service.domain.models.permission.vo.PermissionId;
import com.hieu.auth_service.domain.models.role.Role;
import com.hieu.auth_service.domain.models.user.User;

/**
 * Cross-aggregate authorisation rules (permission/role checks) that span the
 * {@link User}, {@link Role}, and {@link Permission} aggregates.
 *
 * <p>Stateless, framework-free — can run inside any layer or test harness.
 */
public class AuthenticationDomainService {

    public boolean hasPermission(User user, List<Role> userRoles,
                                 List<Permission> allSystemPermissions, String permissionName) {
        Set<String> grantedPermissionIds = extractPermissionIds(userRoles);
        return allSystemPermissions.stream()
                .filter(p -> grantedPermissionIds.contains(p.getId().value()))
               
                .anyMatch(p -> p.getName().value().equals(permissionName));
    }

    public boolean hasRole(User user, List<Role> userRoles, String roleName) {
        return userRoles.stream().anyMatch(r -> r.getName().value().equals(roleName));
    }

    public Set<String> getPermissionNames(User user, List<Role> userRoles,
                                          List<Permission> allSystemPermissions) {
        Set<String> grantedPermissionIds = extractPermissionIds(userRoles);
        return allSystemPermissions.stream()
                .filter(p -> grantedPermissionIds.contains(p.getId().value()))
                .map(p -> p.getName().value())
                .collect(Collectors.toSet());
    }

    public boolean canAccessResource(User user, List<Role> userRoles,
                                     List<Permission> allSystemPermissions,
                                     String resource, String action) {
        Set<String> grantedPermissionIds = extractPermissionIds(userRoles);
        return allSystemPermissions.stream()
                .filter(p -> grantedPermissionIds.contains(p.getId().value()))
                .anyMatch(p -> p.grants(resource, action));
    }

    private Set<String> extractPermissionIds(Collection<Role> roles) {
        return roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(PermissionId::value)
                .collect(Collectors.toSet());
    }
}
