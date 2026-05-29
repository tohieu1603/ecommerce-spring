package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.querys.CheckRoleQuery;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Transactional
public class CheckRoleHandler implements QueryHandler<CheckRoleQuery, Boolean>{
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Boolean handle(CheckRoleQuery query) {
        return userRepository.findByIdWithRoles(UserId.of(query.userId()))
                .map(u -> {
                    var roleId = u.getRoles();
                    if(ObjectUtils.isEmpty(roleId)) return false;

                    return roleRepository.findByIdIn(roleId).stream()
                        .anyMatch(r -> r.getName().value().equals(query.roleName()));
                })
                .orElse(false);
    }
    
}
