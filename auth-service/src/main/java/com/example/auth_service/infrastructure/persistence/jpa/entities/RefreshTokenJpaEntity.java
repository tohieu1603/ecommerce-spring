package com.example.auth_service.infrastructure.persistence.jpa.entities;


import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_rt_user_id", columnList = "user_id"),
    @Index(name = "idx_rt_family", columnList = "family"),
    @Index(name = "idx_rt_revoked_expiry", columnList = "revoked,expiry_date")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenJpaEntity extends BaseManualIdEntity{

    @Column(unique = true, nullable = false, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Token family UUID — all rotated tokens from same login share this */
    @Column(name = "family", nullable = false, length = 100)
    private String family;

    /** Rotation depth within family — root=0, increments on each refresh */
    @Column(name = "generation", nullable = false)
    @Builder.Default
    private Integer generation = 0;

    /** Why the token was revoked (NORMAL, REUSE_DETECTED, FAMILY_REVOKED, USER_INITIATED, EXPIRED) */
    @Column(name = "revoked_reason", length = 50)
    private String revokedReason;

    @Builder
    public RefreshTokenJpaEntity(String id, String token, UserJpaEntity user,
                                 Instant expiryDate, Boolean revoked, Instant createdAt,
                                 Instant revokedAt, String family, Integer generation,
                                 String revokedReason, boolean isNew) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = revoked != null ? revoked : false;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
        this.family = family;
        this.generation = generation != null ? generation : 0;
        this.revokedReason = revokedReason;
        this.isNew = isNew;
    }
}
