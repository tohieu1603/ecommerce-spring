package com.hieu.common.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String path,
    Instant timestamp,
    List<FieldError> fileError,
    String traceId,
    Map<String, Object> details
) {
    public record FieldError(String file, String messgae, Object rejectValue) {}

    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return new ErrorResponse(
            code.code(),
            message == null ? code.name() : message,
            path,
            Instant.now(),
            null, null, null
        );
    }

    public static ErrorResponse validation(String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(
            ErrorCode.VALIDATION_FAILED.code(),
            "Request validation failed",
            path,
            Instant.now(),
            fieldErrors, null, null
        );
    }
}
