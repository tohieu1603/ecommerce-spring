package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public class GoogleTokenInvalidException extends DomainException{
    public GoogleTokenInvalidException(String detail) {
        super("AUTH-1006", "Invalid Google token: " + detail);
    }
}
