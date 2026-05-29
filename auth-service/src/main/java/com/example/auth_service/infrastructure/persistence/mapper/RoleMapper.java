package com.example.auth_service.infrastructure.persistence.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.infrastructure.persistence.jpa.entities.PermissionJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.entities.RoleJpaEntity;


/**
 * Anti-Corruption Layer between as a {@code Role} aggregate and {@link RoleJpaEntity}
 * 
 * <p>Permission associations are represented as a {@code Set<PermissionId>} in the domain but as 
 * a {@code Set<PermissionJpaEntity>} in JPA - the mapper bridges the two.
 */
@Component
public class RoleMapper {
    

    /**
     * Project a domain {@link Role} onto a persistable {@link RoleJpaEntity}.
     * 
     * @param role source domain aggregate
     * @param permission JPA permission entities already resolved by the caller
     * @param isNew true-false
     * @return populated JPA entity
     */
    public RoleJpaEntity toEntity(Role role, Set<PermissionJpaEntity> permission, boolean isNew) {
        if(role == null) return null;

        RoleJpaEntity entity = RoleJpaEntity.builder()
                .id(role.getId().value())
                .name(role.getName().value())
                .description(role.getDescription())
                .permissions(permission)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();

        entity.setNew(isNew);
        return entity;
    }

    public Role toDomain(RoleJpaEntity entity) {
        if(entity == null) return null;
        Set<PermissionId> permissionId = entity.getPermissions().stream()
                .map(p -> PermissionId.of(p.getId()))
                .collect(Collectors.toSet());


        return Role.reconstitute(
            RoleId.of(entity.getId()),
            RoleName.of(entity.getName()),
            entity.getDescription(),
            permissionId,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
