package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.ChangePasswordCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.Password;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.domain.services.PasswordEncodePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChangePasswordCommandHandler implements CommandHandler<ChangePasswordCommand, Void> {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncodePort passwordEncoder;
    private final TokenBlacklistPort tokenBlacklistPort;
    
    @Override
    public Void handle(ChangePasswordCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.changePassword(
                Password.createRaw(command.oldPassword()),
                Password.createRaw(command.newPassword()),
                passwordEncoder);

        userRepository.save(user);
        refreshTokenRepository.revokeAllTokensForUser(UserId.of(command.userId()));

        if(command.currentAccessTokenJti() != null) {
            tokenBlacklistPort.revoke(command.currentAccessTokenJti(),
            command.userId(), command.currentAccessTokenExp(), "PASSWORD_CHANGED");
        }else {
            log.warn("Change passsword: no Jti provider, skipped access-token blacklist for userId{}", command.userId());
        }
        return null;
    }
    
}
