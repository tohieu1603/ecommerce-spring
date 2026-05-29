package com.example.auth_service.domain.models.token.exceptions;

import com.example.auth_service.domain.shared.DomainException;

public final class TokenReuseDetectedException extends DomainException{
    private final String family;
    private final int generation;

    public TokenReuseDetectedException(String family, int generation) {
        super("AUTH-0010", "Token reuse detected in family " + family + " at generation " + generation);
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
