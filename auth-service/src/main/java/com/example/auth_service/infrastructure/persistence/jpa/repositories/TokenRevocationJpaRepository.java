package com.example.auth_service.infrastructure.persistence.jpa.repositories;

import com.example.auth_service.infrastructure.persistence.jpa.entities.TokenRevocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TokenRevocationJpaRepository extends JpaRepository<TokenRevocationJpaEntity, String> {
    /** Returns true when a JWT jti is present in the blacklist (DB fallback path). */
    boolean existsByTokenId(String tokenId);

    /**
     * Fetches all entries whose original token has not yet expired.
     * Used on startup to warm up the Redis blacklist from persistent state.
     */
    List<TokenRevocationJpaEntity> findAllByExpiresAtAfter(Instant now);

    /**
     * Deletes rows for tokens that have already expired naturally.
     * Called by the scheduled cleanup job every hour.
     */
    @Modifying
    @Query("DELETE FROM TokenRevocationEntity t WHERE t.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") Instant now);
}
