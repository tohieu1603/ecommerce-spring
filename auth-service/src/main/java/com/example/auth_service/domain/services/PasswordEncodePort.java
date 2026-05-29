package com.example.auth_service.domain.services;

public interface PasswordEncodePort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
