package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.AssignRoleCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignRoleHandler implements CommandHandler<AssignRoleCommand, Void>{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Void handle(AssignRoleCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(RoleName.of(command.roleName()))
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.assignRoles(role.getId());
        userRepository.save(user);
        return null;
    }


}
