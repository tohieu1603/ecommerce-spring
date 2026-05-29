package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.ChangeAccountStatusCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeAccountStatusHandler implements CommandHandler<ChangeAccountStatusCommand, Void>{

    private final UserRepository userRepository;

    @Override
    public Void handle(ChangeAccountStatusCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        switch (command.transition()) {
            case LOCK -> user.lock();
            case UNLOCK -> user.unlock();
            case ENABLE -> user.enable();
            case DISABLE -> user.disable();
        }
        return null;
    }
    
}
