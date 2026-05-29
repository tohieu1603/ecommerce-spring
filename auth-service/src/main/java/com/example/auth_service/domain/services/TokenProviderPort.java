package com.example.auth_service.domain.services;

import java.time.Instant;
import java.util.Set;

import com.example.auth_service.domain.models.user.User;

public interface TokenProviderPort {

    record IssuedAccessToken(String token, String tokenId, Instant expiresAt, long expiresInSeconds) {}

    record AccessClaims(String tokenId, String userId, String username,
                        int tokenVersion, Set<String> roles, Set<String> permissions, Instant expiresAt) {}

    IssuedAccessToken issueAccessToken(User user, Set<String> roles, Set<String> permissions);

    AccessClaims parseAccessToken(String token);
}
