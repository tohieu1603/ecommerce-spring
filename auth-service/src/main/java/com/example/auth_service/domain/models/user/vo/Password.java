package com.example.auth_service.domain.models.user.vo;

/**
 * Value Object representing a user's password.
 * Immutable and encapsulates validation logic for password strength and format.
 * The 'encoded' flag indicates whether the password is stored in an encoded (hashed) form or as raw text, allowing for flexibility in handling password storage and verification.
 * Designed to be used within the User Aggregate to manage password-related operations and ensure that only valid passwords are created and stored.
 */

public record Password(String value, boolean encoded) {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;


    public Password(String value, boolean encoded) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if(!encoded) {
            validateRawPassword(value);
        }
        this.value = value;
        this.encoded = encoded;
    }
    public static Password createRaw(String password) {
        return new Password(password, false);
    }
    public static Password createEncoded(String password) {
        return new Password(password, true);
    }
    private void validateRawPassword(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password must be at most " + MAX_LENGTH + " characters long");
        }
        if(!containsDigit(password)) {
            throw new IllegalArgumentException("Password must contain digits");
        }
        if(!containsLetter(password)) {
            throw new IllegalArgumentException("Password must contain letters");
        }

    }
    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }
    private boolean containsLetter(String password) {
        return password.chars().anyMatch(Character::isLetter);
    }
    public boolean needsEncoding() {
        return !encoded;
    }
    @Override
    public String toString() {
        return "Password{encoded=" + encoded + "}";
    }

}
