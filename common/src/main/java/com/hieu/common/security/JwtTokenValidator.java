package com.hieu.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtTokenValidator {
    private final SecretKey secretKey;

    private static final String PLACEHOLDER_SECRET_PREFIX = "hieu-too";

    public JwtTokenValidator(String secret) {
        if(secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt secret must be at least 32 characters (HS256 requirement)");
        }
        if(secret.startsWith(PLACEHOLDER_SECRET_PREFIX) && isProdProfile()) {
              throw new IllegalStateException(
                    "Default placeholder jwt secret detected with prod profile active — " +
                    "set the JWT_SECRET environment variable to a strong secret");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isProdProfile() {
        String profile = System.getProperty("spring.profiles.active", "")
            + "," + System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "");
        
        return profile.contains("prod");
    }

    public JwtTokenValidator(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

      public boolean validateSignature(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses all claims; throws {@link JwtException} on invalid tokens.
     *
     * @param token raw JWT string
     * @return parsed claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    /** @return JWT {@code sub} claim (username). */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** @return {@code userId} claim (UUID String — matches auth-service's domain). */
    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    /** @return {@code jti} claim — used as blacklist key. */
    public String extractTokenId(String token) {
        return parseClaims(token).getId();
    }

    /** @return {@code tokenVersion} claim, or {@code 0} when absent (legacy tokens). */
    public int extractTokenVersion(String token) {
        Integer version = parseClaims(token).get("tokenVersion", Integer.class);
        return version != null ? version : 0;
    }

    /** @return {@code roles} claim; empty list when absent. */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        List<String> roles = parseClaims(token).get("roles", List.class);
        return roles != null ? List.copyOf(roles) : List.of();
    }

    /**
     * @param token raw JWT string
     * @return {@code true} when the token's exp claim is in the past (treats unparseable as expired)
     */
    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
