package com.example.auth_service.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.auth_service.infrastructure.persistence.jpa.entities.RoleJpaEntity;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String> {
    Optional<RoleJpaEntity> findByName(String name);

    boolean existsByName(String name);

    Set<RoleJpaEntity> findByIdIn(Set<String> ids);

    @Query("SELECT r FROM RoleJpaEntity r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<RoleJpaEntity> findByIdWithPermissions(@Param("id") String id);

    @Query("SELECT r FROM RoleJpaEntity r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<RoleJpaEntity> findByNameWithPermissions(@Param("name") String name);

    @Query("SELECT DISTINCT r FROM RoleJpaEntity r LEFT JOIN FETCH r.permissions")
    List<RoleJpaEntity> findAllWithPermissions();
}
