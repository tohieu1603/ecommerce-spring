package com.example.auth_service.infrastructure.persistence.jpa.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "token_revocations", indexes = {
        @Index(name = "idx_token_revocations_token_id", columnList = "token_id"),
        @Index(name = "idx_token_revocations_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class TokenRevocationJpaEntity extends BaseManualIdEntity{



    /** Owner of the revoked token (for bulk-revoke queries). */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Timestamp when revocation was recorded. */
    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    /**
     * Original expiry of the JWT.
     * Used to set Redis TTL and to purge stale rows after tokens naturally expire.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Human-readable reason for revocation.
     * Possible values: LOGOUT, ADMIN_REVOKE, SECURITY_BREACH, PASSWORD_CHANGE
     */
    @Column(name = "reason", nullable = false, length = 50)
    private String reason;

    public static TokenRevocationJpaEntity forRevocation(String tokenId, String userId,
                                                         Instant expiresAt, String reason) {
        TokenRevocationJpaEntity entity = new TokenRevocationJpaEntity();
        entity.id = tokenId;
        entity.userId = userId;
        entity.createdAt = Instant.now();
        entity.revokedAt = Instant.now();
        entity.expiresAt = expiresAt;
        entity.reason = reason;
        entity.isNew = true;
        return entity;
    }
}
