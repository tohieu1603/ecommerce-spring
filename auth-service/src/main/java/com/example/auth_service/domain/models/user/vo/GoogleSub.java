package com.example.auth_service.domain.models.user.vo;

public record GoogleSub(String value) {
    
    public GoogleSub {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("GoogleSub cannot be null or empty");
        }

        value = value.trim();

        if(value.length() > 500) {
            throw new IllegalArgumentException("GoogleSub too long: " + value.length());
        }
    }

    public static GoogleSub of(String value) {
        return new GoogleSub(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
