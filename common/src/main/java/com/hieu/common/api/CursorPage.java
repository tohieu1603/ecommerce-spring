package com.hieu.common.api;

import java.util.List;

public record CursorPage<T>(
    List<T> items,
    String nextCursor,
    int size,
    boolean hasNext
) {
    public CursorPage {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public <T> CursorPage<T> of(List<T> items, String nextCursor, int size) {
        return new CursorPage<>(items, nextCursor, size, nextCursor != null);
    }
}
