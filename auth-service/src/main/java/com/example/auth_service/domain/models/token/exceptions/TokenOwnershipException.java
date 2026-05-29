package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class TokenOwnershipException extends DomainException{
    public TokenOwnershipException(String userId) {
        super("AUTH-0009", "Token does not belong to user " + userId);
    }
}
