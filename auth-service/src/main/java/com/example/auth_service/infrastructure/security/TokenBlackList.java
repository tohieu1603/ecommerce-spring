package com.example.auth_service.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.infrastructure.persistence.jpa.entities.TokenRevocationJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.TokenRevocationJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hybrid Redis + DB blacklist for access tokens — implements {@link TokenBlacklistPort}.
 *
 * <p>Revoke flow:
 * <ol>
 *   <li>Redis {@code SET blacklist:{jti} = reason} with TTL = remaining lifetime
 *       (best-effort; failure is non-fatal).</li>
 *   <li>Always persist to {@code token_revocations} table — the source of truth.</li>
 * </ol>
 *
 * <p>Check flow: Redis-first (fast-path), falls back to DB on Redis outage.
 *
 * <p>On startup the service warms Redis from DB so a fresh Redis instance cannot silently
 * bypass active revocations persisted before the restart. A scheduled hourly job purges
 * rows whose JWT has expired naturally.
 *
 * <p>{@code userId} is stored as String (UUID) — previous {@code Long} typing was
 * incompatible with the domain model.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TokenBlackList implements TokenBlacklistPort{

    private static final String BACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;
    private final TokenRevocationJpaRepository revocationRepository;


        /**
     * Revokes an access token by its jti claim. Idempotent — re-revoking an already-revoked
     * token simply refreshes the Redis TTL and writes a new DB row iff one doesn't exist.
     *
     * @param tokenId   JWT jti
     * @param userId    owning user UUID (String)
     * @param expiresAt original JWT expiry instant
     * @param reason    free-form reason code (LOGOUT | ADMIN_REVOKE | SECURITY_BREACH | PASSWORD_CHANGED)
     */
    @Override
    @Transactional
    public void revoke(String tokenId, String userId, Instant expiresAt, String reason) {
        long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        if(ttlSeconds > 0) {
            try {
                redisTemplate.opsForValue()
                    .set(BACKLIST_PREFIX + tokenId, reason, Duration.ofSeconds(ttlSeconds));
            } catch (Exception e) {
                log.error("Redis unavailable - failed to blacklist tokenId {} for userId {}: {}", tokenId, userId, e.getMessage());
            }
            revocationRepository.save(
                TokenRevocationJpaEntity.forRevocation(tokenId, userId, expiresAt, reason)
            );
        }
    }



    @Override
    public boolean isRevoked(String tokenId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BACKLIST_PREFIX + tokenId));
        } catch (Exception e) {
            log.error("Redis unavailable - failed to check if tokenId {} is revoked: {}", tokenId, e.getMessage());
            return revocationRepository.existsById(tokenId);
        }
    }
    /** Rebuilds Redis from DB on startup so a fresh Redis cannot silently bypass active revocations. */
    public void warmUpRedisFromDb() {
        try {
            List<TokenRevocationJpaEntity> active = revocationRepository.findAllByExpiresAtAfter(Instant.now());

            active.forEach(row -> {
                long ttl = Duration.between(Instant.now(), row.getExpiresAt()).getSeconds();

                if(ttl > 0) {
                    redisTemplate.opsForValue().set(
                            BACKLIST_PREFIX + row.getId(),
                            row.getReason(),
                            Duration.ofSeconds(ttl));
                }
            });
            log.info("Blacklist warm-up: restore {} active revocation(s) in to Redis", active.size());
        } catch (Exception e) {
            log.error("Failed to warm up Redis from DB: {}", e.getMessage());
        }
    }
    
    @Scheduled(fixedDelay = 3_600_000L)
    @Transactional
    public void cleanupExpiredRevocations() {
        revocationRepository.deleteByExpiresAtBefore(Instant.now());
        log.debug("Purged expired token revocation row");
    }
}