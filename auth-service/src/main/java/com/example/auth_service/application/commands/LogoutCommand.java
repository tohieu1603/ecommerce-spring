package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;

public record LogoutCommand(String accessToken, String refreshToken) implements Command<Void> {}
