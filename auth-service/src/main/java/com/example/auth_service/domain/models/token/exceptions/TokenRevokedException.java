package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.models.token.vo.RevokedReason;
import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public final class TokenRevokedException extends DomainException{
    private final String tokenId;
    private final RevokedReason reason;

    public TokenRevokedException(String tokenId, RevokedReason reason) {
        super(ErrorCode.TOKEN_REVOKED.code(), "Token " + tokenId + " has been revoked: " + reason);
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
