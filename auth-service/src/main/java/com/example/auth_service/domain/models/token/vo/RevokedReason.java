package com.example.auth_service.domain.models.token.vo;

/**
 * Value Object representing the reason why a token was revoked. This allows us to capture more context
 * around token revocations, which can be useful for security monitoring, analytics, and providing more informative error messages
 * to clients. By encapsulating the reason in a dedicated value object, we can also add behavior related to revocation reasons in
 * the future (e.g. categorizing reasons as security-related vs user-initiated, determining if a reason should trigger family
 * revocation, etc.) without having to change the structure of our events or exceptions that reference it.
 */
public record RevokedReason(String reason) {


    public static final RevokedReason NORMAL          = new RevokedReason("NORMAL");
    public static final RevokedReason REUSE_DETECTED  = new RevokedReason("REUSE_DETECTED");
    public static final RevokedReason FAMILY_REVOKED  = new RevokedReason("FAMILY_REVOKED");
    public static final RevokedReason USER_INITIATED  = new RevokedReason("USER_INITIATED");
    public static final RevokedReason EXPIRED         = new RevokedReason("EXPIRED");

    public RevokedReason(String reason) {
        if(reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason cannot be null or empty");
        }
        this.reason = reason.trim().toUpperCase();
    }
    public static RevokedReason of(String reason) {
        if(reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason cannot be null or empty");
        }
        return switch (reason.trim().toUpperCase()) {
            case "NORMAL"           -> NORMAL;
            case "REUSE_DETECTED"   -> REUSE_DETECTED;
            case "FAMILY_REVOKED"   -> FAMILY_REVOKED;
            case "USER_INITIATED"   -> USER_INITIATED;
            case "EXPIRED" -> EXPIRED;
            default -> new RevokedReason(reason);
        };
    }
    public boolean isSecurityRelated() {
        return this.equals(REUSE_DETECTED) || this.equals(FAMILY_REVOKED);
    }
    public boolean isNormal() {
        return this.equals(NORMAL);
    }
    public boolean shouldRevokeFamily() {
        return this.equals(FAMILY_REVOKED);
    }
}