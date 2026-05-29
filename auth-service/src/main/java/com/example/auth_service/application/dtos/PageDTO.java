package com.example.auth_service.application.dtos;

import java.util.List;

public record PageDTO<T>(
    List<T> items,
    String nextCursor,
    int pageSize,
    long totalElements
) {
    public static <T> PageDTO<T> of(List<T> items, String nextCursor, int pageSize, long totalElements) {
        return new PageDTO<>(List.copyOf(items), nextCursor, pageSize, totalElements);
    }
}
