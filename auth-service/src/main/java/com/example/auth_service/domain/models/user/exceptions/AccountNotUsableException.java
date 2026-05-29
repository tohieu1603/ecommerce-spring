package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public final class AccountNotUsableException extends DomainException {

    public enum Reason {
        DISABLED(ErrorCode.ACCOUNT_DISABLED, "Account is disabled"),
        LOCKED(ErrorCode.ACCOUNT_LOCKED, "Account is locked"),
        EXPIRED(ErrorCode.ACCOUNT_EXPIRED, "Account is expired"),
        CREDENTIALS_EXPIRED(ErrorCode.CREDENTIALS_EXPIRED, "Credentials are expired");

        private final ErrorCode errorCode;
        private final String message;

        Reason(ErrorCode errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String code() { return errorCode.code(); }
        public String message() { return message; }
    }
    private final Reason reason;

    public AccountNotUsableException(Reason reason) {
        super(reason.code(), reason.message());
        this.reason = reason;
    }
    
    public Reason reason() { return reason;}
}
