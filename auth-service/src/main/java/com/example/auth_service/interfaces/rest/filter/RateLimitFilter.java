package com.example.auth_service.interfaces.rest.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * C5: Per-IP rate limit for /login and /register using Bucket4j in-memory buckets.
 *
 * <p>Limits: login -> 5 req/min, register -> 3 req/min. Exceeding limit returns HTTP 429
 * with a JSON error body matching the global errors schema 
 */


@Component
@EnableConfigurationProperties(RateLimitProperties.class)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter{

    private static final String LOGIN_PATH    = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";

    private static final String KEY_PREFIX = "rate_limit";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final RateLimitProperties props;
    private final Set<String> whitelist;

    private static final RedisScript<List> INCR_AND_EXPIRE = new DefaultRedisScript<>(
            """
            local c = redis.call('INCR', KEYS[1])
            if c == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
                return {c, tonumber(ARGV[1])}
            end
            local ttl = redis.call('TTL', KEYS[1])
            return {c, ttl}
            """,
            List.class);

    public RateLimitFilter(StringRedisTemplate redis,
                           ObjectMapper objectMapper,
                           RateLimitProperties props) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.props = props;
        this.whitelist = props.whiteListSet();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        RateLimitProperties.Endpoint config = configFor(request.getRequestURI());
        if(config == null) {
            chain.doFilter(request, response);

            return;
        }

        String ip = clientIp(request);
        if (whitelist.contains(ip)) {
            chain.doFilter(request, response);

            return;
        }

        Result result = consume(keyFor(request.getRequestURI(), ip), config);
        if(result.exceeded()) {
            writeTooManyRequets(response, result.retryAfterSeconds());
            return;
        }

        chain.doFilter(request, response);
    }

        private RateLimitProperties.Endpoint configFor(String path) {
        if (LOGIN_PATH.equals(path))    return props.login();
        if (REGISTER_PATH.equals(path)) return props.register();
        return null;
    }

    private static String keyFor(String path, String ip) {
        String tag = LOGIN_PATH.equals(path) ? "login" : "regiter";

        return KEY_PREFIX + tag + ":" + ip;
    }

    private Result consume(String key, RateLimitProperties.Endpoint cfg) {
        try {

            @SuppressWarnings("unchecked")
            List<Long> reply = redis.execute(
                INCR_AND_EXPIRE,
                List.of(key),
                String.valueOf(cfg.window().toSeconds())
            );

            if(reply == null || reply.size() < 2) {
                return Result.allowed(cfg.window());
            }

            long count = reply.get(0);
            long ttl = reply.get(1);

            return new Result(count > cfg.limit(), Math.max(ttl, 1));
        } catch (Exception e) {
                        log.warn("Redis unavailable for rate limit (key={}); failing open: {}",
                    key, e.getMessage());
            return Result.allowed(cfg.window());
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if(xff != null || !xff.isBlank()) {
            int comma = xff.indexOf(",");
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequets(HttpServletResponse response, long retryAfterSeconds) throws IOException{
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        objectMapper.writeValue(response.getWriter(), Map.of(
            "code", "AUTH-1014",
            "message", "Too many requests",
            "retryAfterSeconds", retryAfterSeconds
        ));
    }

    /** Result of the rate-limit check. */
    private record Result(boolean exceeded, long retryAfterSeconds) {
        static Result allowed(Duration window) {
            return new Result(false, window.toSeconds());
        }
    }
    
}
