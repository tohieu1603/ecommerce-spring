package com.example.auth_service.application.handlers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;

import com.example.auth_service.application.commands.AssignRoleCommand;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

public class AssignRoleHandlerTest {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AssignRoleHandler handler;


    private final String USER_ID = UUID.randomUUID().toString();
    
    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        handler = new AssignRoleHandler(userRepository, roleRepository);
    }

    @Test
    void shouldThrowUserNotFound_whenUserMissing() {
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new AssignRoleCommand(USER_ID, "ROLE_ADMIN")))
            .isInstanceOf(UserNotFoundException.class);
        
        Mockito.verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenRoleMissing() {
        User user = Mockito.mock(User.class);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findByName(RoleName.of("ROLE_UNKNOWN")))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> handler.handle(new AssignRoleCommand(USER_ID, "ROLE_UNKNOWN")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ROLE_UNKNOWN");
        
        Mockito.verify(userRepository, never()).save(any());
    }

    @Test
    void shouldAssignRoleAndPersisUser_whenBothExist() {
        User user = Mockito.mock(User.class);
        Role role = Mockito.mock(Role.class);
        RoleId roleId = RoleId.generate();
        Mockito.when(role.getId()).thenReturn(roleId);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findByName(any())).thenReturn(Optional.of(role));
        
        handler.handle(new AssignRoleCommand(USER_ID, "ROLE_ADMIN"));

        Mockito.verify(user).assignRoles(roleId);
        Mockito.verify(userRepository).save(user);
    }
}