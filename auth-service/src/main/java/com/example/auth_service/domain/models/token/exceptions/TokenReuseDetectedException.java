package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public final class TokenReuseDetectedException extends DomainException{
    private final String family;
    private final int generation;

    public TokenReuseDetectedException(String family, int generation) {
        super(ErrorCode.TOKEN_REUSE_DETECTED.code(), "Token reuse detected in family " + family + " at generation " + generation);
        this.family = family;
        this.generation = generation;
    }

    public String family() {
        return family;
    }

    public int generation() {
        return generation;
    }
}
