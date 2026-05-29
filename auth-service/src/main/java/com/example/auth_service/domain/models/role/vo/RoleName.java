package com.example.auth_service.domain.models.role.vo;


public record RoleName(String value) {
    private static final String ROLE_PREFIX = "ROLE_";

    public RoleName {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or blank");
        }

        String trimmed = value.trim().toUpperCase();
        if (!trimmed.startsWith(ROLE_PREFIX)) {
            trimmed = ROLE_PREFIX + trimmed;
        }

        value = trimmed;
    }

    public static RoleName of(String value) {
        return new RoleName(value);
    }

    public static RoleName admin() {
        return new RoleName(ROLE_PREFIX + "ADMIN");
    }

    public static RoleName user() {
        return new RoleName(ROLE_PREFIX + "USER");
    }

    public String getSimpleName() {
        return value.substring(ROLE_PREFIX.length());
    }

    @Override
    public String toString() {
        return value;
    }
}
