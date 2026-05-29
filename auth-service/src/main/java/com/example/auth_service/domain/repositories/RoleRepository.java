package com.example.auth_service.domain.repositories;

import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.permission.vo.PermissionName;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.role.vo.RoleName;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Repository interface for managing Role entities. This defines the contract for how roles are stored, retrieved, and queried in the system.
 * By abstracting the data access layer behind this interface, we can easily swap out different implementations
 * (e.g. in-memory, JPA, MongoDB) without affecting the rest of the application. The methods provided allow for basic CRUD operations, as well as more specific queries based on role attributes like name and permissions. This repository is a key part of the domain layer, as roles are essential for implementing authorization logic and enforcing access control throughout the application.
 * Note: The save method is used for both creating new roles and updating existing ones. The findByIdWithPermissions and findByNameWithPermissions methods allow for eager loading of permissions associated with a role, which can be useful for authorization checks without needing additional queries. The delete method allows for removing roles from the system, while the findAll method provides a way to retrieve all roles, which can be useful for administrative interfaces or role management features.
 * Transactional annotations can be added to the implementation of this interface to ensure that operations that modify the database are executed within a transaction, providing atomicity and consistency guarantees.
 * Example usage:
 * Role adminRole = new Role(new RoleId(UUID.randomUUID()), new RoleName("ADMIN"), Set.of(permission1, permission2));
 * roleRepository.save(adminRole);
 * Optional<Role> foundRole = roleRepository.findByNameWithPermissions(new RoleName("ADMIN"));
 * if (foundRole.isPresent()) {
 *     Role role = foundRole.get();
 *     // Use role and its permissions for authorization checks
 * }
 */

public interface RoleRepository {

    /**
     * Save a role (insert or update)
     */
    Role save(Role role);

    /**
     * Find role by ID
     */
    Optional<Role> findById(RoleId roleId);

    /**
     * Find role by name
     */
    Optional<Role> findByName(RoleName roleName);

    /**
     * Find multiple roles by IDs
     */
    List<Role> findByIdIn(Set<RoleId> roleIds);

    /**
     * Find role with permissions loaded
     */
    Optional<Role> findByIdWithPermissions(RoleId roleId);

    /**
     * Find role by name with permissions loaded
     */
    Optional<Role> findByNameWithPermissions(RoleName roleName);

    /**
     * Check if role name exists
     */
    boolean existsByName(RoleName roleName);

    /**
     * Delete role
     */
    void delete(Role role);

    /**
     * Find all roles
     */
    List<Role> findAll();
}
