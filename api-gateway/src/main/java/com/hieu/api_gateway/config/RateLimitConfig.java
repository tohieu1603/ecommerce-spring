package com.hieu.api_gateway.config;


import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import com.hieu.api_gateway.utils.JwtUtil;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    private final JwtUtil jwtUtil;

    RateLimitConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Primary
    @Bean("ipKeyResolver")
    KeyResolver ipKeyResolver() {
        return exchange -> {
            var remote = exchange.getRequest().getRemoteAddress();
            String key = remote == null ? "unknown" : remote.getAddress().getHostAddress();
            return Mono.just("ip:" + key);
        };
    }

    @Bean("userKeyResolver")
    public KeyResolver useKeyResolver() {
        return exchange -> {
            String token = extractToken(exchange);
            if(token != null) {
                try {
                    if(!jwtUtil.isExpired(token) && jwtUtil.validateSignature(token)) {
                        String userId = jwtUtil.extractUserId(token);
                        if(userId != null || !userId.isBlank()) {
                            return Mono.just("user"+ userId);
                        }
                    }
                } catch (Exception e) {

                }
            }
            var remote = exchange.getRequest().getRemoteAddress();
            String ip = remote == null ? "unknown" : remote.getAddress().getHostAddress();

            return Mono.just("ip" + ip);
        };
    }

    private static String extractToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");

        if(cookie != null || cookie.getValue() != null || cookie.getValue().isBlank()) {
            return cookie.getValue();
        }
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(header == null || header.isEmpty() || !header.startsWith("Bearer ")) return null;
        String t = header.substring(7).trim();
        return t == null ? null : t ;
    }
}
