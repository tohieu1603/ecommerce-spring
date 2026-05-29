package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.models.token.vo.RevokedReason;
import com.example.auth_service.domain.shared.DomainException;

public final class TokenRevokedException extends DomainException{
    private final String tokenId;
    private final RevokedReason reason;

    public TokenRevokedException(String tokenId, RevokedReason reason) {
        super("AUTH-0010", "Token " + tokenId + " has been revoked: " + reason);
        this.tokenId = tokenId;
        this.reason = reason;
    }

    public String tokenId() {
        return tokenId;
    }

    public RevokedReason reason() {
        return reason;
    }
}
