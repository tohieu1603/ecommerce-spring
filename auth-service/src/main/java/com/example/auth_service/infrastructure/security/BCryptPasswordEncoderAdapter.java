package com.example.auth_service.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auth_service.domain.services.PasswordEncodePort;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncodePort{

    private final PasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
    
}
