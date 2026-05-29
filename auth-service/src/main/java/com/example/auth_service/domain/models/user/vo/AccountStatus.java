package com.example.auth_service.domain.models.user.vo;

import java.time.Instant;

/**
 * Value Object representing the security and lifecycle status of a User Account.
 * Immutable and encapsulates all relevant flags and timestamps related to account usability.
 * Designed to be used within the User Aggregate to determine if an account is active and can be authenticated.
 */

public record AccountStatus (
    boolean enabled,
    boolean accountNonExpired,
    boolean accountNonLocked,
    boolean credentialsNonExpired,
    Instant lastLogin
) {

    public static AccountStatus of(
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Instant lastLogin
    ) {
        return new AccountStatus(
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                lastLogin
        );
    }
    public static AccountStatus createActive() {
        return new AccountStatus(
                true,
                true,
                true,
                true,
                null
        );
    }
    public static AccountStatus createDisabled() {
        return new AccountStatus(
                false,
                true,
                true,
                true,
                null
        );
    }
    public static AccountStatus createLocked() {
        return new AccountStatus(true, true, false, true, null);
    }
    public AccountStatus lock() {
        return new AccountStatus(
                enabled,
                accountNonExpired,
                false,
                credentialsNonExpired,
                lastLogin
        );
    }
    public AccountStatus unlock() {
        return new AccountStatus(
                enabled,
                accountNonExpired,
                true,
                credentialsNonExpired,
                lastLogin
        );
    }
    public AccountStatus disable() {
        return new AccountStatus(
                false,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                lastLogin
        );
    }
    public AccountStatus enable() {
        return new AccountStatus(
                true,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                lastLogin
        );
    }
    public boolean isActive() {
        return enabled && accountNonExpired && accountNonLocked && credentialsNonExpired;
    }
    public AccountStatus withLastLogin(Instant time) {
        return new AccountStatus(
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                time
        );
    }
}
