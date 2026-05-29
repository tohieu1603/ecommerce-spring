package com.example.auth_service.interfaces.rest.filter;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rate-limit")
public record RateLimitProperties(
        Endpoint login,
        Endpoint register,
        List<String> whiteList
) {

    public RateLimitProperties {
        login    = (login    != null) ? login    : new Endpoint(5, Duration.ofMinutes(1));
        register = (register != null) ? register : new Endpoint(5, Duration.ofMinutes(1));
        whiteList = (whiteList != null) ? whiteList : List.of();
        
    }
    
    /** Convenience: O(1) lookup instead of scanning the list per request. */
    public Set<String> whiteListSet() {
        return Set.copyOf(whiteList); 
    }

    public record Endpoint(int limit, Duration window) {

        public Endpoint {
            if (limit  <= 0)      throw new IllegalArgumentException("limit must be > 0");
            if (window == null || window.isZero() || window.isNegative()) {
                throw new IllegalArgumentException("window must be positive");
            }
        }
    }

    
}
