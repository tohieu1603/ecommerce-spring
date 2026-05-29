package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class UserAlreadyExistsException extends DomainException{

    public UserAlreadyExistsException(String reason) {
        super("AUTH-0006", "User already exists: " + reason);
    }
}
