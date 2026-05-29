package com.example.auth_service.infrastructure.messaging;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.auth_service.application.port.RolePermissionCachePort;
import com.example.auth_service.domain.events.DomainEvent;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.event.PermissionGrantedEvent;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.repositories.PermissionRepository;
import com.example.auth_service.domain.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RolePermissionCacheInvalidator {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionCachePort cachePort;


    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void onPermissionGranted(PermissionGrantedEvent event) {
        refresh(event);
    }
    

    private void refresh(DomainEvent event) {
        String roleId = event.aggregateId();
        try {
            Role role = roleRepository.findById(RoleId.of(roleId)).orElse(null);
            if(role == null) {
                log.debug("Role {} no longer exists; leaving cache as-is", roleId);
                return;
            }

            Set<String> permissions = role.getPermissions().isEmpty()
                ? Set.of()
                : permissionRepository.findByIdIn(role.getPermissions()).stream()
                    .map(p -> p.getName().value())
                    .collect(Collectors.toSet());
            cachePort.put(role.getName().value(), permissions);
            log.debug("Refreshed cache for role {} with permissions {}", roleId, permissions.size());

        } catch (Exception e) {
            log.error("Error occurred while refreshing cache for role {}", roleId, e.getMessage());
            cachePort.evictAll();
        }
    }
}
