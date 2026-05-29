package com.example.auth_service.infrastructure.persistence.mapper;


import org.springframework.stereotype.Component;

import com.example.auth_service.domain.models.permission.Permission;
import com.example.auth_service.domain.models.permission.vo.PermissionId;
import com.example.auth_service.infrastructure.persistence.jpa.entities.PermissionJpaEntity;


@Component
public class PermissionMapper {


    public PermissionJpaEntity toEntity(Permission permission, boolean isNew) {
        if(permission == null) return null;

        PermissionJpaEntity entity = PermissionJpaEntity.builder()
                .id(permission.getId().value())
                .name(permission.getName().value())
                .resource(permission.getName().resource())
                .action(permission.getName().action())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();

        entity.setNew(isNew);
        return entity;
    }

    public Permission toDomain(PermissionJpaEntity entity) {
        return Permission.reconstitute(
                PermissionId.of(entity.getId()),
                entity.getResource(),
                entity.getAction(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateJpaEntity(Permission permission, PermissionJpaEntity entity) {
        entity.setName(permission.getName().value());
        entity.setResource(permission.getName().resource());
        entity.setAction(permission.getName().action());
        entity.setDescription(permission.getDescription());
        entity.setUpdatedAt(permission.getUpdatedAt());
    }
}
