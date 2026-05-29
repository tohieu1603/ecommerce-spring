package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public class OAuthEmailNotVerifiedException extends DomainException{
    public OAuthEmailNotVerifiedException() {
        super(ErrorCode.OAUTH_EMAIL_UNVERIFIED.code(),
            "OAuth provider email is not verified"
        );
    }
}
