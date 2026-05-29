package com.example.auth_service.application.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ValidationException extends ApplicationException{
    private final Map<String, String> fieldErrors;

    /**
     * Creates a validation exception with only a top-level message.
     *
     * @param message human-readable summary
     */
    public ValidationException(String message) {
        super("APP-400", message);
        this.fieldErrors = new LinkedHashMap<>();
    }

    /**
     * Creates a validation exception carrying field-level errors.
     *
     * @param message     top-level summary
     * @param fieldErrors map of {@code field → error message}; a defensive copy is stored
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("APP-400", message);
        this.fieldErrors = fieldErrors == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldErrors);
    }

    /** @return unmodifiable view of collected field errors */
    public Map<String, String> fieldErrors() {
        return Map.copyOf(fieldErrors);
    }

    /** @return {@code true} if at least one field error was reported */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
