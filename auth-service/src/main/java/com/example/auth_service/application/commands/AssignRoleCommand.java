package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;

public record AssignRoleCommand(String userId, String roleName)implements Command<Void> {}
