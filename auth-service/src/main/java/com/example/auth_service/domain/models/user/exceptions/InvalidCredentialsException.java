package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class InvalidCredentialsException extends DomainException{
    public InvalidCredentialsException() {
        super("AUTH-0005", "Invalid username or password");
    }
}
