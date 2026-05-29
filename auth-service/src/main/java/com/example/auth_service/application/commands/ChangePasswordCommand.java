package com.example.auth_service.application.commands;

import java.time.Instant;

import com.example.auth_service.application.common.Command;

public record ChangePasswordCommand(
    String userId,
    String oldPassword,
    String newPassword,
    String currentAccessTokenJti,
    Instant currentAccessTokenExp
) implements Command<Void> {}
