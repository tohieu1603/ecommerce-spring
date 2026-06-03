package com.example.auth_service.domain.services;

import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.exceptions.TokenOwnershipException;
import com.example.auth_service.domain.models.token.exceptions.TokenReuseDetectedException;
import com.example.auth_service.domain.models.token.vo.RevokedReason;
import com.example.auth_service.domain.models.token.vo.TokenFamily;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;


/**
 * Cross-aggregate token business rules: issue, rotate, reuse-detection, family, revocation.
 *
 * <p>Stateless and framework-free - lives in the domain layer so the same rules apply
 * whether callers come from REST, gRPC, Kafka, or test.
 */

public class TokenDomainService {
    /**
     * Issue the very first refresh token of a login session
     * 
     * @throws IllegalStateException when the user cannot currently authenticate
     */
    public RefreshToken issueForUser(User user, int expiryDays) {
        if(!user.isActive()) {
            throw new IllegalStateException("User is not active");
        }

        return RefreshToken.create(user.getId(), expiryDays);
    }

    /**
     * Rotate: revoke old, issue next-generation.
     * Presented-but-already-revoked tokens are treated as theft - the full family is
     * revoked and {@link TokenReuseDetectedEXception} is raised.
     */

    public RefreshToken rotate(RefreshToken old, int expiryDays, RefreshTokenRepository repository) {
        if(old.isReuseAttempt()) {
            revokeFamily(old.getFamily(), repository);

            throw new TokenReuseDetectedException(
                old.getFamily().value(),
                old.getGeneration().value()
            );
        }
        old.verifyValidity();
        old.revoke(RevokedReason.NORMAL);
        repository.save(old);

        return RefreshToken.rotate(old, expiryDays);
    }

    /** Marks every live token in a family revoked with reason FAMILY_REVOKED. */
    public void revokeFamily(TokenFamily family, RefreshTokenRepository repository) {
        repository.findByFamily(family).stream()
        .filter(t -> !t.isRevoke())
        .forEach(t -> {
            t.revoke(RevokedReason.FAMILY_REVOKED);
            repository.save(t);
        });
    }

    public void validateOwnership(RefreshToken token, UserId userId) {
        if(!token.belongsTo(userId)) {
            throw new TokenOwnershipException(userId.value());
        }
    }
}

