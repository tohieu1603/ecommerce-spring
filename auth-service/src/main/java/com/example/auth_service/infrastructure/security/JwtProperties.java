package com.example.auth_service.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 *Externalised JWT configuration
 *
 * <p>Validated in the compact contructor so the application fails fast on misconfiguration.
 * a wake secret or zero/negative TTL surfaces at startup rather than during at the first login
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,  /** HMAC secret, secret >= 32 char for HS256 signing */
    long accessExpirationSeconds, /** access token TTL in seconds, default is 900 (15 minutes) */
    int refreshExpirationDays, /** refresh token TTL in days, default is 7 */
    String issuer /** JWT issuer, default is "auth-service" */
) {
    
    public JwtProperties {
        if(secret == null || secret.length() < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 characters long for HS256 signing");
        }

        if(accessExpirationSeconds <= 0) accessExpirationSeconds = 900L; // 15 minutes
        if(refreshExpirationDays <= 0) refreshExpirationDays = 7; // 7
        if(issuer == null || issuer.isBlank()) issuer = "auth-service";
    }
}
