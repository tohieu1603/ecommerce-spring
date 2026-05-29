package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class UserNotFoundException extends DomainException{
    
    public UserNotFoundException(String lookup) {
        super("AUTH-0007", "User not found: " + lookup);
    }
}
