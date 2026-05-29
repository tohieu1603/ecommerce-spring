package com.hieu.common.api;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String code,
    String message,
    T data,
    Instant timestamp,
    String traceId
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(false, "Ok", null, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, "Ok", message, data, Instant.now(), null);
    }
}
