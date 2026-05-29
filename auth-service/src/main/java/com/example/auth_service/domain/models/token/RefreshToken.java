package com.example.auth_service.domain.models.token;


import com.example.auth_service.domain.events.DomainEvent;
import com.example.auth_service.domain.models.token.events.TokenCreatedEvent;
import com.example.auth_service.domain.models.token.vo.*;
import com.example.auth_service.domain.models.user.vo.UserId;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.auth_service.domain.models.token.events.TokenRevokedEvent;
import com.example.auth_service.domain.shared.AggregateRoot;

/**
 * RefreshToken aggregate root.
 * 
 * <p> Implements the <b>Refresh Token Rotation + Family Revocation</b> pattern:
 * <ul>
 *  <li>Each login issues a root token (generation 0) with a fresh family id. </li>
 *  <li>On every refresh the presented token is revoked and a new token is issued in same
 *      family with generation = old + 1. </li>
 * <li>If an already-revoked token is re-presented, {@link #isReuseAttempt()} flags
 *     theft and the code {@code  TokenDomainService} cascades a family revocation. </li>
 * </ul>
 */

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"id", "UserId", "revoke", "expried", "generation", "family", "revoked"})
public class RefreshToken extends AggregateRoot{

    @EqualsAndHashCode.Include
    private TokenId id;
    private TokenValue value;
    private UserId userId;
    private TokenExpiry expiry;
    private boolean revoke;
    private TokenFamily family;
    private GenerationNumber generation;
    private RevokedReason reason;
    private Instant createdAt;
    private Instant revokedAt;

    public List<DomainEvent> events = new ArrayList<>();

    private RefreshToken() {
        this.revoke = false;
    }

    public static RefreshToken create(
            UserId userId,
            int expiredDays
    ) {
        RefreshToken token = new RefreshToken();
        token.value = TokenValue.generate();
        token.userId = userId;
        token.expiry = TokenExpiry.fromDaysFromNow(expiredDays);
        token.revoke = false;
        token.family = TokenFamily.generate();
        token.generation = GenerationNumber.root();
        token.createdAt = Instant.now();

        token.registerEvent(new TokenCreatedEvent(
            token.id.value(), userId.value(), token.family.value(),
            token.generation.value(), token.expiry.expiry()));

        return token;
    }
    public static RefreshToken rotate(
           RefreshToken oldToken,
           int expiredDays
    ) {
        RefreshToken token = new RefreshToken();
        token.value = oldToken.value;
        token.value = TokenValue.generate();
        token.userId = oldToken.userId;
        token.expiry = TokenExpiry.fromDaysFromNow(expiredDays);
        token.revoke = false;
        token.generation = oldToken.generation.next();
        token.createdAt = Instant.now();

        token.registerEvent(new TokenCreatedEvent(
                token.id.value(), token.userId.value(), token.family.value(),
                token.generation.value(), token.expiry.expiry()));

        return token;
    }
    /** Rebuilds aggregate state from persistence. Domain events are intentionally NOT replayed. */
    public static RefreshToken reconstitute(TokenId id, TokenValue value, UserId userId,
                                            TokenExpiry expiry, boolean revoked,
                                            Instant createdAt, Instant revokedAt,
                                            TokenFamily family, GenerationNumber generation,
                                            RevokedReason reason) {
        RefreshToken t = new RefreshToken();
        t.id = id;
        t.value = value;
        t.userId = userId;
        t.expiry = expiry;
        t.revoke = revoked;
        t.createdAt = createdAt;
        t.revokedAt = revokedAt;
        t.family = family;
        t.generation = generation;
        t.reason = reason;
        return t;
    }

    /** Throws when the token cannot be used - distinct exceptions for revoked vs expired */
    public void verifyValidity() {
        if(revoke) {
            throw new IllegalArgumentException("Token is revoked" +
                    (reason != null ?  "Reason: " + reason.reason() : ""));
        }
        if(expiry.isExpired()) {
            throw new IllegalArgumentException("Token is expired:" + expiry.expiry());
        }
    }
    
    public boolean isValid() {
        return !revoke && !expiry.isValid();
    }

    public void revoke(RevokedReason reason) {
        Objects.requireNonNull(reason, "RevokedReason must not be null");
        if(revoke) return;
        this.revoke = true;
        this.reason = reason;
        this.revokedAt = Instant.now();

        registerEvent(new TokenRevokedEvent(
            id != null ? id.value() : null,
            userId.value(),
            family.value(),
            reason.reason()
        ));
    }

    public void revoke() {
        revoke(RevokedReason.NORMAL);
    }
    public boolean isReuseAttempt() {
        return !revoke && !expiry.isValid();
    }

    public boolean belongsTo(UserId userId) {
        return this.userId.equals(userId);
    }
    public boolean willExpireSoon(long seconds) {
        return expiry.willExpireWithin(seconds);
    }
    public long getRemainingSeconds() {
        if(!isValid()) {
            return 0;
        }
        return expiry.getRemainingSeconds();
    }
    public boolean wasRevokedForSecurity() {
        return reason != null && reason.isSecurityRelated();
    }
}
