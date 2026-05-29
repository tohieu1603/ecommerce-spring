package com.example.auth_service.domain.models.permission.vo;

public record PermissionName(String resource, String action, String value) {

    public PermissionName(String resource, String action) {
        this(
                resource != null ? resource.trim().toUpperCase() : null,
                action != null ? action.trim().toUpperCase() : null,
                (resource != null && action != null)
                        ? (resource.trim().toUpperCase() + "_" + action.trim().toUpperCase())
                        : null
        );

        if (this.resource == null || this.resource.isEmpty()) {
            throw new IllegalArgumentException("Permission resource cannot be empty");
        }
        if (this.action == null || this.action.isEmpty()) {
            throw new IllegalArgumentException("Permission action cannot be empty");
        }
    }
    public static PermissionName of(String resource, String action) {
        return new PermissionName(resource, action);
    }
    public static PermissionName fromString(String permission) {
        if(permission == null || permission.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be null or blank");
        }

        String[] parts = permission.trim().toUpperCase().split("_");
        if(parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid permission format, Excepted: RESOURCE_ACTION, got: " + permission);
        }
        return new PermissionName(parts[0], parts[1]);

    }
    public boolean isForResource(String resource) {
        return this.resource.equalsIgnoreCase(resource);
    }
    public boolean allowsAction(String action) {
        return this.action.equalsIgnoreCase(action);
    }
    @Override
    public String toString() {
        return value;
    }
}
