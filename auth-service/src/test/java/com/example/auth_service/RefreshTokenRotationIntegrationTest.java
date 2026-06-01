package com.example.auth_service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import com.example.auth_service.application.commands.LoginCommand;
import com.example.auth_service.application.commands.RefreshTokenCommand;
import com.example.auth_service.application.commands.RegisterUserCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.vo.TokenValue;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshTokenRotationIntegrationTest extends AbstractIntegrationTest {
    
    private final CommandHandler<RegisterUserCommand, AuthResponseDTO> registerHandler;
    private final CommandHandler<RefreshTokenCommand, AuthResponseDTO> refreshHandler;
    private final CommandHandler<LoginCommand, AuthResponseDTO> loginHandler;
    private final RefreshTokenRepository refreshTokenRepository;


    @Test
    void refreshToken_rotatesTokenWithinSameFamily_andRevokesOldToken() {

        /** Register a new user */
        AuthResponseDTO registered = registerHandler.handle(new RegisterUserCommand(
            "newUser", "hieu@gmail1314.com", "Hieu1603", "Hieu", "To0"
        ));

        String oldRefreshToken = registered.refreshToken();

        AuthResponseDTO rotated = refreshHandler.handle(new RefreshTokenCommand(oldRefreshToken));

        assertThat(rotated.refreshToken()).isNotEqualTo(registered.accessToken());
        assertThat(rotated.refreshToken()).isNotEqualTo(oldRefreshToken);

        RefreshToken oldToken = refreshTokenRepository.findByTokenValue(TokenValue.of(oldRefreshToken))
            .orElseThrow(() -> new RuntimeException("Old refresh token not found in repository"));
        
        assertThat(oldToken.isRevoke()).isTrue();

        RefreshToken newToken = refreshTokenRepository.findByTokenValue(TokenValue.of(rotated.refreshToken()))
            .orElseThrow(() -> new RuntimeException("New refresh token not found in repository"));
        
        assertThat(newToken.getFamily()).isEqualTo(oldToken.getFamily());
        assertThat(newToken.isRevoke()).isFalse();
        assertThat(newToken.getGeneration().value()).isEqualTo(oldToken.getGeneration().value() + 1);

    }

        @Test
    void login_issuesNewFamilyStartingAtGenerationZero() {
        registerHandler.handle(new RegisterUserCommand(
                "famuser", "fam@example.com", "P@ssw0rd123", "Fam", "User"));

        AuthResponseDTO loggedIn = loginHandler.handle(new LoginCommand("famuser", "P@ssw0rd123"));

        RefreshToken token = refreshTokenRepository.findByTokenValue(TokenValue.of(loggedIn.refreshToken()))
                .orElseThrow();
        // Fresh login = new session = new family starting at generation 0.
        assertThat(token.getGeneration().isRoot()).isTrue();
    }
}
