package com.example.auth_service.domain.models.user.vo;

import java.util.regex.Pattern;

/**
 * Value Object representing a validated email address.
 * Immutable and encapsulates validation logic to ensure only valid email addresses are created.
 * Provides utility methods to access local part and domain of the email.
 */

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public  Email(String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is null or empty");
        }
        String trimValue = value.trim().toLowerCase();

        if(!EMAIL_PATTERN.matcher(trimValue).matches()) {
            throw new IllegalArgumentException("Invalid email");
        }

        this.value = trimValue;
    }
    public static Email of(String email) {
        return new Email(email);
    }
    public String getDomain() {
        return value.substring(value.indexOf("@") + 1);
    }
    public String getLocalPart() {
        return value.substring(0,value.indexOf("@"));
    }

}
