package com.example.auth_service.domain.shared;

/**
 * Root of the domain exception hierarchy
 *
 * <p>Every domain error carries a stable {@link #code()} that clients can branch on
 * without parsing human-readable messages. Codes follow the pattern
 * {@code <DOMAIN>-<NNNN>} (e.g. {@code AUTH-0001}) and must never be renamed - only added
 *
 * <p>Sealed so infrastructure/application layers can use exhaustive switches when
 * mapping domain errors HTTP/gRPC responses, but domain layer can still define as many specific exceptions as needed
 */

public abstract class DomainException extends RuntimeException {

    private final String code;

    protected  DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public final String code() {
        return code;
    }
}

