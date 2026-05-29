package com.example.auth_service.domain.repositories;

import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.permission.vo.PermissionName;

import java.util.*;


/**
 * Repository interface for managing Permission entities. This defines the contract for how permissions are stored, retrieved, and queried in the system.
 * By abstracting the data access layer behind this interface, we can easily swap out
 * different implementations (e.g. in-memory, JPA, MongoDB) without affecting the rest of the application.
 * The methods provided allow for basic CRUD operations, as well as more specific queries based on permission 
 * attributes like name, resource, and action. This repository is a key part of the domain layer, as permissions are essential 
 * for implementing authorization logic and enforcing access control throughout the application.
 */
public interface PermissionRepository {

    /**
     * Save a permission (insert or update)
     */
    Permission save(Permission permission);

    /**
     * Find permission by ID
     */
    Optional<Permission> findById(PermissionId permissionId);

    /**
     * Find permission by name
     */
    Optional<Permission> findByName(PermissionName permissionName);

    /**
     * Find multiple permissions by IDs
     */
    List<Permission> findByIdIn(Set<PermissionId> permissionIds);

    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by action
     */
    List<Permission> findByAction(String action);

    /**
     * Find permission by resource and action
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if permission name exists
     */
    boolean existsByName(PermissionName permissionName);

    /**
     * Delete permission
     */
    void delete(Permission permission);

    /**
     * Find all permissions
     */
    List<Permission> findAll();

}
