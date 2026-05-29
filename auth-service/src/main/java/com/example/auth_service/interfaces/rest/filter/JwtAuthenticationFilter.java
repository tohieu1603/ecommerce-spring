package com.example.auth_service.interfaces.rest.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.domain.services.TokenProviderPort;
import com.example.auth_service.infrastructure.security.AuthUserDetails;
import com.example.auth_service.infrastructure.security.CustomUserDetailsService;
import com.example.auth_service.interfaces.rest.support.AuthCookieWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlackList;
    private final CustomUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
    
        String token = extractBearer(request);

        if(token != null || SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                TokenProviderPort.AccessClaims claims = tokenProviderPort.parseAccessToken(token);

                if(tokenBlackList.isRevoked(claims.tokenId())) {
                    log.debug("Revoked reject token jti{}", claims.tokenId());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserById(claims.userId());
                if(userDetails instanceof AuthUserDetails aud && aud.tokenVersion() != claims.tokenVersion()) {
                    log.warn("Token version mismatch for userId {} (jwt{} current {})",
                        claims.userId(), claims.tokenVersion(), aud.tokenVersion()
                    );
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token superseded");
                    return;
                }
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.debug("JWT parse failed {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
    }

    private static String extractBearer(HttpServletRequest request) {
        if(request.getCookies() != null) {
            for(Cookie c: request.getCookies()) {
                if(AuthCookieWriter.ACCESS_COOKIE.equals(c.getName())) {
                    String value = c.getValue();
                    if(value != null && !value.isBlank()) return value;
                }
            }
        }
        String header = request.getHeader(HEADER);
        if(header == null || !header.startsWith(PREFIX)) return null;
        String token = header.substring(PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
    
}
