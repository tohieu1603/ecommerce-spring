package com.example.auth_service.interfaces.rest.dtos;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.auth_service.infrastructure.security.AuthUserDetails;

public record AuthMeResponse(
    String userId,
    String username,
    String email,
    int tokenVersion,
    Set<String> authorities
) {
    public static AuthMeResponse from(AuthUserDetails principal) {
        Set<String> granted = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
        return new AuthMeResponse(
            principal.userId(),
            principal.getUsername(),
            principal.email(),
            principal.tokenVersion(),
            granted);
    }
}
