package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;
import com.example.auth_service.application.dtos.AuthResponseDTO;

public record LoginCommand(String usernameOrEmail, String password) implements Command<AuthResponseDTO> {}
