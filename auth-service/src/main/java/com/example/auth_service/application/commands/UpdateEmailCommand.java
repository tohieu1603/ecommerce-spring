package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;
import com.example.auth_service.application.dtos.UserDTO;

/**
 * Changes the current user's email address.
 *
 * @param userId   subject user id (from authenticated context)
 * @param newEmail desired new email address
 */
public record UpdateEmailCommand(String userId, String newEmail) implements Command<UserDTO> {
}