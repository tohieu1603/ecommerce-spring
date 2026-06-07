package com.hieu.api_gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.hieu.common.security.AuthHeader;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = UUID.randomUUID().toString();
        long started = System.currentTimeMillis();

        log.info("[{}] {} {}", correlationId, request.getMethod(), request.getURI());

         ServerHttpRequest mutated = request.mutate()
                .header(AuthHeader.CORRELATION_ID, correlationId)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build())
                .then(Mono.fromRunnable(() -> {
                    long elapsed = System.currentTimeMillis() - started;
                    log.info("[{}] {} {} -> {} ({}ms)",
                            correlationId,
                            request.getMethod(), request.getURI(),
                            exchange.getResponse().getStatusCode(),
                            elapsed);
                }));
    }

}
