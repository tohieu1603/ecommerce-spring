package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public class OAuthAccountAlreadyLinkedException extends DomainException{
    public OAuthAccountAlreadyLinkedException(String provider) {
        super(ErrorCode.OAUTH_ACCOUNT_LINKED.code(),
            "Account already linked with another " + provider + "identity"
        );
    }
}
