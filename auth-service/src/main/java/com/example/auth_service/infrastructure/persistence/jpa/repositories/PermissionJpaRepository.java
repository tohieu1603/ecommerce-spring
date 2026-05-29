package com.example.auth_service.infrastructure.persistence.jpa.repositories;

import com.example.auth_service.infrastructure.persistence.jpa.entities.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, String> {
    Optional<PermissionJpaEntity> findByName(String name);

    List<PermissionJpaEntity> findByIdIn(Set<String> ids);

    List<PermissionJpaEntity> findByResource(String resource);

    List<PermissionJpaEntity> findByAction(String action);


    Optional<PermissionJpaEntity> findByResourceAndAction(String resource, String action);

    boolean existsByName(String name);
}
