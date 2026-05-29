package com.example.auth_service.infrastructure.cache;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.auth_service.application.port.RolePermissionCachePort;
import com.example.auth_service.domain.repositories.PermissionRepository;
import com.example.auth_service.domain.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Redis-backed implementation of {@link RolePermissionCachePort}.
 *
 * <p>Storage shape: Redis SET at key {@code role_permissions:{ROLE_NAME}} holding
 * permission names. Using SET (not String) avoids separator-collision bugs and enables
 * O(1) membership checks if callers later need per-permission lookup.
 *
 * <p>TTL = 5 minutes. If an invalidation event is missed (e.g. Kafka consumer offline),
 * the cache self-heals within one TTL window.
 *
 * <p>Warm-up on {@link ApplicationReadyEvent}: loads every role into the cache so the
 * first authenticated request doesn't pay a cold-miss DB query.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisRolePermissionCacheAdapter implements RolePermissionCachePort {

    static final String KEY_PREFIX = "role_permissions:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Set<String> get(String name) {
        try {

            Set<String> members = redisTemplate.opsForSet().members(KEY_PREFIX + name);
            log.info("Retrieved permissions for role: {}", name);
            return (members == null || members.isEmpty()) ? null : members;

        }catch (Exception e) {
            log.error("Error fetching role permissions from Redis cache for role '{}'", name, e);
            return null; // Cache miss on error
        }
    }

    @Override
    public void put(String roleName, Set<String> permissionName) {
        if(permissionName == null || permissionName.isEmpty()) {
            evict(roleName);
            return;
        }
        try {
            String key = KEY_PREFIX + roleName;
            redisTemplate.delete(key);
            redisTemplate.opsForSet().add(key, permissionName.toArray(String[]::new));
            redisTemplate.expire(key, TTL);
            log.info("Cached permissions for role: {}", roleName);
        } catch (Exception e) {
            log.error("Error caching role permissions in Redis for role '{}'", roleName, e);
        }

    }

    @Override
    public void evict(String roleName) {
        try {
            String key = KEY_PREFIX + roleName;
            redisTemplate.delete(key);
            log.info("Evicted cache for role: {}", roleName);
        } catch (Exception e) {
            log.error("Error evicting cache for role '{}'", roleName, e);
        }
    }

    @Override
    public void evictAll() {
        try {
            var keys = new HashSet<String>();
            try (var cursor = redisTemplate.getConnectionFactory().getConnection().keyCommands()
                    .scan(ScanOptions.scanOptions()
                            .match(KEY_PREFIX + "*").count(100).build())) {
                cursor.forEachRemaining(b -> keys.add(new String(b)));
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error evicting all cache entries", e);
        }
    }
    
}
