package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public class OAuthEmailNotVerifiedException extends DomainException{
    public OAuthEmailNotVerifiedException() {
        super("AUHT-0008",
            "OAuth provider email is not verified"
        );
    }
}
