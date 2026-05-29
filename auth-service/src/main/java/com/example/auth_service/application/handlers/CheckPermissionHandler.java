package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.querys.CheckPermissionQuery;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.PermissionRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly= true)
public class CheckPermissionHandler implements QueryHandler<CheckPermissionQuery, Boolean>{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Boolean handle(CheckPermissionQuery query) {
        return userRepository.findByIdWithRoles(UserId.of(query.userId()))
                .map(u -> hasPermission(u.getRoles(), query.permissionName()))
                .orElse(false);
    }
    private boolean hasPermission(Set<RoleId> roles, String targetPermission) {
        if(ObjectUtils.isEmpty(roles)) return false;

        Set<PermissionId> permissionIds = roleRepository.findByIdIn(roles).stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
        
        if(ObjectUtils.isEmpty(permissionIds)) return false;

        return permissionRepository.findByIdIn(permissionIds).stream()
                .anyMatch(p -> p.getName().value().equals(targetPermission));
    }
    
}
