package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.querys.GetUserByIdQuery;
import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.PermissionRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly= true)
public class GetUserByIdHandler implements QueryHandler<GetUserByIdQuery, UserDTO>{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserDtoMapper mapper;

    @Override
    public UserDTO handle(GetUserByIdQuery query) {
        User user = userRepository.findByIdWithRoles(UserId.of(query.userId()))
                .orElseThrow(() -> new UserNotFoundException(query.userId()));

        var roles = roleRepository.findByIdIn(user.getRoles());
        var permissionId = roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .collect(Collectors.toSet());
        
        List<Permission> permissions = permissionId.isEmpty() ? List.of() : permissionRepository.findByIdIn(permissionId);

        return mapper.toDto(user, roles, List.copyOf(permissions));
    }

    
}
