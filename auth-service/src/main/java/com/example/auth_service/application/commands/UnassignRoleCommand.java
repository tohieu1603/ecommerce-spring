package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;

public record UnassignRoleCommand(String userId, String roleName) implements Command<Void> {}
