package com.hieu.api_gateway.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hieu.common.security.JwtTokenValidator;

import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    private JwtTokenValidator validator;

    @PostConstruct
    void init() {
        this.validator = new JwtTokenValidator(secret);
    }

    public boolean validateSignature(String token) { return validator.validateSignature(token); }
    public boolean isExpired(String token)          { return validator.isExpired(token); }

    public String extractUsername(String token)     { return validator.extractUsername(token); }
    public String extractUserId(String token)       { return validator.extractUserId(token); }
    public String extractTokenId(String token)      { return validator.extractTokenId(token); }
    public int    extractTokenVersion(String token) { return validator.extractTokenVersion(token); }

}
