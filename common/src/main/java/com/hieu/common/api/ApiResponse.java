package com.hieu.common.api;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @param success   {@code true} for 2xx responses, {@code false} for handled errors
 * @param code      stable machine-readable code ({@code "OK"} or {@code "AUTH-1001"}, …)
 * @param message   optional human-readable message
 * @param data      payload (present when {@code success=true})
 * @param timestamp server-side instant the response was built
 * @param traceId   correlation id from the incoming request, if present
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Instant timestamp,
        String traceId) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(false, "Ok", null, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, "Ok", message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null, Instant.now(), null);
    }

    public ApiResponse<T> withTraceId(String traceId) {
        return new ApiResponse<>(success, code, message, data, timestamp, traceId);
    }
}
