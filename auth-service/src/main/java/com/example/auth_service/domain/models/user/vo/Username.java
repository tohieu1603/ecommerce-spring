package com.example.auth_service.domain.models.user.vo;

import java.util.regex.Pattern;

/**
 * Value Object representing a username.
 * Immutable and encapsulates validation logic to ensure that only valid usernames are created.
 * Usernames must be between 5 and 30 characters long and can only contain letters,
 * digits, underscores, and hyphens. Designed to be used within the User Aggregate to represent
 * the user's unique identifier and ensure that only valid usernames are created and stored.
 */

public record Username(String value) {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 30;
    private static final Pattern USER_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    public Username(String value) {
        if(value == null || value.trim().isEmpty()){
            throw new IllegalArgumentException("value cannot be null or empty");
        }

        String trim = value.trim();

        if(trim.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("value cannot be less than " + MIN_LENGTH);
        }
        if(trim.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("value cannot be more than " + MAX_LENGTH);
        }
        if(!USER_PATTERN.matcher(trim).matches()){
            throw new IllegalArgumentException("value is not a valid username");
        }
        this.value = value;
    }
    public static Username of(String value) {
        return new Username(value);
    }
    @Override
    public String toString() {
        return value;
    }

}
