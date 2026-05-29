package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.UnassignRoleCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UnassignRoleHandler implements CommandHandler<UnassignRoleCommand, Void>{
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Override
    public Void handle(UnassignRoleCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Role role = roleRepository.findByName(RoleName.of(command.roleName()))
                .orElseThrow(() -> new IllegalArgumentException("Role name not found " + command.roleName()));
        
        user.assignRoles(role.getId());
        userRepository.save(user);

        return null;
        
    }
    
}
