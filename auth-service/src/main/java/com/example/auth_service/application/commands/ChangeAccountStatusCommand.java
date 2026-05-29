package com.example.auth_service.application.commands;

import com.example.auth_service.application.common.Command;

public record ChangeAccountStatusCommand(String userId, Transition transition)
        implements Command<Void> {

    public enum Transition { LOCK, UNLOCK, ENABLE, DISABLE }
}
