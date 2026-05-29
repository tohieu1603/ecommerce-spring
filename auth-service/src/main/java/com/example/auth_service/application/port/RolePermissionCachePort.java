package com.example.auth_service.application.port;

import java.util.Set;

import com.example.auth_service.domain.models.role.vo.RoleName;


/**
 * Outbound port: role→permission names cache.
 *
 * <p>Populated on startup from the role registry; used by
 * {@code CustomUserDetailsService} to resolve effective permissions during JWT
 * authentication without hitting Postgres every request. Cache is invalidated
 * whenever a Kafka {@code PermissionGranted/Revoked} event fires.
 *
 * <p>Infrastructure supplies a Redis adapter; the interface stays framework-free so
 * tests can swap in an in-memory stub.
 */
public interface RolePermissionCachePort {
    
    /** 
     * Return the cache permission names for a role , or {@code null} on cache miss
     * 
     * @param name canonical role name (e.g. {@code ROLE_USER})
     * @return set of permission names, or {@code null} when the cache has no entry.
     */
    Set<String> get(String name);
    
    /**
     * Stores (or overwrites) the cache entry for a role.
     * @param roleName canonical role name
     * @param permissionName permission granted through this role (defensive copy stored)
     */
    void put(String roleName, Set<String> permissionName);

    /** Remove a single role from the cache */
    void evict(String roleName);

    /** Wipes the entire cache - used on bulk permission changes */
    void evictAll();
}
