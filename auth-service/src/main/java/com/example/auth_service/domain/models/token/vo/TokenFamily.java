package com.example.auth_service.domain.models.token.vo;

import java.util.UUID;

/**
 * Value Object representing a token family. A token family is a group of tokens that are related to each other, 
 * typically because they were issued as part of the same authentication session or flow. For example, an access 
 * token and a refresh token that were issued together would belong to the same token family. By grouping tokens 
 * into families, we can implement features like family revocation, where if one token in the family is 
 * revoked (e.g. due to suspected compromise), all other tokens in the same family can also be revoked to prevent unauthorized access. 
 * This value object encapsulates the logic for creating and validating token family identifiers, which are typically UUIDs 
 * to ensure uniqueness across the system.
 */

public record TokenFamily(String value) {

    public TokenFamily(String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Token family value cannot be null or empty");
        }
        UUID.fromString(value.trim());
        this.value = value;
    }
    public static TokenFamily of(String value) {
        return new TokenFamily(value);
    }
    public static TokenFamily generate() {
        return new TokenFamily(UUID.randomUUID().toString());
    }
    public boolean isSameFamily(TokenFamily other) {
        return other != null && value.equals(other.value);
    }
}