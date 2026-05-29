package com.example.auth_service.domain.models.token.vo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Value Object representing the expiry of a token. Encapsulates the logic for determining 
 * if a token is expired, how much time is left until expiry, and provides factory methods for creating expiries
 * based on common timeframes (e.g. days or hours from now). Designed to be used within the Token Aggregate to manage
 * token lifecycles and ensure that expiry logic is consistently applied across the domain
 */

public record TokenExpiry(Instant expiry) {

    public TokenExpiry(Instant expiry) {
        if(expiry == null) {
            throw new IllegalArgumentException("Expiry value must not be null.");
        }
        this.expiry = expiry;
    }
    public static TokenExpiry of(Instant expiry) {
        return new TokenExpiry(expiry);
    }
    /** Creates a new TokenExpiry instance based on the number of days from now */
    public static TokenExpiry fromDaysFromNow(int days) {
        if(days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0.");
        }
        return new TokenExpiry(Instant.now().plus(days, ChronoUnit.DAYS));
    }
    public static TokenExpiry fromHoursFromNow(int hours) {
        if(hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than 0.");
        }
        return new TokenExpiry(Instant.now().plus(hours, ChronoUnit.HOURS));
    }
    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }
    public boolean isValid() {
        return !isExpired();
    }
    
    /** Returns the number of seconds remaining until the token expires */
    public long getRemainingSeconds() {
        if(isExpired()) {
            return 0;
        }
        return Instant.now().until(expiry, ChronoUnit.SECONDS);
    }
    public boolean willExpireWithin(long seconds) {
        return getRemainingSeconds() <=  seconds;
    }
    @Override
    public String toString() {
        return "TokenExpiry{"
                + "expiry=" + expiry +
                ", remainingSeconds=" + getRemainingSeconds() +
                ", isExpired=" + isExpired() +
                "}";
    }
}
