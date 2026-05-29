package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.LogoutCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.vo.RevokedReason;
import com.example.auth_service.domain.models.token.vo.TokenValue;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.services.TokenProviderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles {@link LogoutCommand}: revokes the refresh token and blacklists the access token
 * so subsequent requests with the same credentials fail immediately.
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogoutHandler implements CommandHandler<LogoutCommand, Void>{

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    public Void handle(LogoutCommand command) {

        /** Step1: revoke refresh token */
       refreshTokenRepository.findByTokenValue(TokenValue.of(command.refreshToken()))
            .ifPresent(this::revokeRefreshToken);

        /** Step2: blacklist access token */
        // H5: Blacklist access token in a separate try-catch AFTER refresh revoke has committed.
        // An expired/malformed access token must NOT roll back the already-committed refresh revoke.
        if(command.accessToken() != null && !command.accessToken().isBlank()) {
            try {
                var claims = tokenProviderPort.parseAccessToken(command.accessToken());
                tokenBlacklistPort.revoke(claims.tokenId(), claims.userId(), claims.expiresAt(), "LOGOUT");
            } catch (Exception e) {
                log.error("Error occurred while parsing access token", e);
            }
        }
        return null;
    
    }

    private void revokeRefreshToken(RefreshToken t) {
        t.revoke(RevokedReason.USER_INITIATED);
        refreshTokenRepository.save(t);
    }
}
