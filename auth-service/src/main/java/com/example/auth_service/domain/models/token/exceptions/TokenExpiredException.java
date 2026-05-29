package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class TokenExpiredException extends DomainException{
    private final String tokenId;

    public TokenExpiredException(String tokenId) {
        super("AUTH-0008", "Token has expired: " + tokenId);
        this.tokenId = tokenId;
    }

    public String tokenId() {
        return tokenId;
    }
}
