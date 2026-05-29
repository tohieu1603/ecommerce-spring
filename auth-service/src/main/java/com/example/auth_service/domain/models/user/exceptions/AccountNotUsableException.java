package com.example.auth_service.domain.models.user.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class AccountNotUsableException extends DomainException {

    public enum Reason {
        DISABLED("AUTH-0001", "Account is disabled"),
        LOCKED("AUTH-0002", "Account is locked"),
        EXPIRED("AUTH-0003", "Account is expired"), 
        CREDENTIALS_EXPIRED("AUTH-0004", "Credentials are expired");

        private final String code;
        private final String message;

        Reason(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String code() { return code; }
        public String message() { return message; }
    }
    private final Reason reason;

    public AccountNotUsableException(Reason reason) {
        super(reason.code(), reason.message());
        this.reason = reason;
    }
    
    public Reason reason() { return reason;}
}
