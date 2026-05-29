package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public class OAuthAccountAlreadyLinkedException extends DomainException{
    public OAuthAccountAlreadyLinkedException(String provider) {
        super("AUTH-0007",
            "Account already linked with another " + provider + "identity"
        );
    }
}
