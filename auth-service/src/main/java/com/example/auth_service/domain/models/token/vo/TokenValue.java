package com.example.auth_service.domain.models.token.vo;


import java.util.UUID;

/**
 * Value Object representing the unique value of a token. This class encapsulates the logic for generating and validating token values,
 * which are typically long random strings that serve as credentials for authentication and authorization. By using a dedicated value 
 * object for the token value, we can ensure that all tokens in the system adhere to a consistent format and validation 
 * logic, which helps improve security and maintainability. The TokenValue class provides factory methods for generating new token 
 * values (e.g. using UUIDs) and creating instances from existing strings, while also enforcing non-null and non-empty constraints to 
 * prevent invalid tokens from being created.
 */

public record TokenValue(String value) {

    public TokenValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TokenValue cannot be null or empty");
        }

        this.value = value.trim();
    }
    public static TokenValue of(String value) {
        return new TokenValue(value);
    }
    public static TokenValue generate() {
        return new TokenValue(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "TokenValue{" +value.substring(0, Math.min(8, value.length())) + "...}";
    }
}