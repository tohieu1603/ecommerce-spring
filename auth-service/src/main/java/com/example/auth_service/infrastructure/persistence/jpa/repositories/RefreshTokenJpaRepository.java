package com.example.auth_service.infrastructure.persistence.jpa.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_service.infrastructure.persistence.jpa.entities.RefreshTokenJpaEntity;

import jakarta.persistence.LockModeType;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, String> {

    Optional<RefreshTokenJpaEntity> findByToken(String token);

    List<RefreshTokenJpaEntity> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshTokenEntity t WHERE t.token = :token")
    Optional<RefreshTokenJpaEntity> findByTokenForUpdate(@Param("token") String token);

    @Query("SELECT t FROM RefreshTokenEntity t WHERE t.user.id = :userId AND t.revoked = false AND t.expiryDate > :now")
    List<RefreshTokenJpaEntity> findValidTokenByUserId(@Param("userId") String userId, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiryDate < :now")
    int deleteExpriedToken(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revoked = true, t.revokedAt = :now, t.reason = 'PASSWORD_CHANGED' WHERE t.user.id =:userId AND t.revoked = false")
    void revokeAllTokenForUser(@Param("userId") String userId, @Param("now") Instant now);

    void deleteByUserId(String userId);

    List<RefreshTokenJpaEntity> findByFamily(String family);
}
