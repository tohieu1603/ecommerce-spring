package com.example.auth_service;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.example.auth_service.application.commands.LoginCommand;
import com.example.auth_service.application.commands.RegisterUserCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterLoginIntegration extends AbstractIntegrationTest {
    private final CommandHandler<RegisterUserCommand, AuthResponseDTO> registerHandler;
    private final CommandHandler<LoginCommand, AuthResponseDTO> loginHandler;

    @Test
    void registerAndLogin_success() {
        AuthResponseDTO registered = registerHandler.handle(new RegisterUserCommand(
            "userRegisternow", "hieutt@gmail.com", "Hieu1603", "HT" ,"TH"
        ));

        assertThat(registered.user().username()).isEqualTo("userRegisternow");
        assertThat(registered.accessToken()).isNotBlank();
        assertThat(registered.refreshToken()).isNotBlank();

        AuthResponseDTO loggedin = loginHandler.handle(new LoginCommand("userRegisternow", "Hieu1603"));

        assertThat(loggedin.accessToken()).isNotBlank();
        assertThat(loggedin.refreshToken()).isNotBlank();
        assertThat(loggedin.accessToken()).isNotEqualTo(registered.accessToken());
        assertThat(loggedin.refreshToken()).isNotEqualTo(registered.refreshToken());
        assertThat(loggedin.user().username()).isEqualTo("userRegisternow");
        assertThat(loggedin.user().id()).isEqualTo(registered.user().id());
    }
}
