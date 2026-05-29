package com.example.auth_service.domain.repositories;

import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.vo.TokenFamily;
import com.example.auth_service.domain.models.token.vo.TokenId;
import com.example.auth_service.domain.models.token.vo.TokenValue;
import com.example.auth_service.domain.models.user.vo.UserId;
import java.util.*;

/**
 * Repository interface for managing RefreshToken entities. This defines the contract for how refresh tokens 
 * are stored, retrieved, and queried in the system.
 * By abstracting the data access layer behind this interface, we can easily swap out different 
 * implementations (e.g. in-memory, JPA, MongoDB) without affecting the rest of the application
 * The methods provided allow for basic CRUD operations, as well as more specific queries based on 
 * token attributes like user ID, token value, and token family.
 * This repository is a key part of the domain layer, as refresh tokens are essential for 
 * implementing secure authentication flows and enabling features like token revocation and reuse detection.
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findById(TokenId tokenId);

    Optional<RefreshToken> findByTokenValue(TokenValue tokenValue);

    /**
     * Same as {@link #findByTokenValue} but acquires a pessimistic write lock on the
     * row. Use this in the refresh-token rotation flow to serialize concurrent
     * refreshes of the same token — without locking, two parallel requests both read
     * a still-valid token and each issue a new access token (double session).
     */
    Optional<RefreshToken> findByTokenValueForUpdate(TokenValue tokenValue);

    List<RefreshToken> findByUserId(UserId userId);

    List<RefreshToken> findValidTokensByUserId(UserId userId);

    /** Find all tokens in a token family (for reuse detection / family revocation) */
    List<RefreshToken> findByFamily(TokenFamily family);

    void delete(RefreshToken token);

    void deleteByUserId(UserId userId);

    int deleteExpiredTokens();

    void revokeAllTokensForUser(UserId userId);
}
