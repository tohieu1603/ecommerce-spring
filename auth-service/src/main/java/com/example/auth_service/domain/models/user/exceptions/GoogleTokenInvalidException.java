package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public class GoogleTokenInvalidException extends DomainException{
    public GoogleTokenInvalidException(String detail) {
        super(ErrorCode.OAUTH_TOKEN_INVALID.code(), "Invalid Google token: " + detail);
    }
}
