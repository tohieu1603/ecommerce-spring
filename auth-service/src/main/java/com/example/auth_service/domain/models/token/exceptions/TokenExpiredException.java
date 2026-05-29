package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;
public final class TokenExpiredException extends DomainException{
    private final String tokenId;

    public TokenExpiredException(String tokenId) {
        super(ErrorCode.TOKEN_EXPIRED.code(), "Token has expired: " + tokenId);
        this.tokenId = tokenId;
    }

    public String tokenId() {
        return tokenId;
    }
}
