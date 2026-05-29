package com.example.auth_service.application.common;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public final class CursorCodec {
    private static final String DELIM = "|";

    private CursorCodec() {}

    public record Cursor(Instant createdAt, String id) {
        public Cursor {
            Objects.requireNonNull(createdAt, "createdAt not null");
            if(id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Cursor id cannot be null");
            }
        }
    }
    
    public static String encode(Instant createdAt, String id) {
        if(createdAt == null || id == null) return null;

        long epochMicros = createdAt.getEpochSecond() * 1_000_000L + createdAt.getNano() / 1_000;
        String raw = epochMicros + DELIM + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
    
    public static Cursor decode(String opaque) {
        if(opaque == null || opaque.isBlank()) return null;

        try {
            String raw = new String(Base64.getDecoder().decode(opaque), StandardCharsets.UTF_8);
            int split = raw.indexOf(DELIM);
            if(split < 0) throw new IllegalArgumentException("missiing delimiter");

            long epochMicros = Long.parseLong(raw.substring(0,split));
            String id = raw.substring(split + DELIM.length());
            Instant ts = Instant.ofEpochSecond(
                    epochMicros / 1_000_000L,
                    (epochMicros % 1_000_000) * 1_000L
            );
        
            return new Cursor(ts, id);
        } catch (Exception e) {
            
            throw new IllegalArgumentException("Invalid cursor: " + e.getMessage());
        }
    }
}
