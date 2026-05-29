package com.example.auth_service.infrastructure.persistence.jpa.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_service.infrastructure.persistence.jpa.entities.UserJpaEntity;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String > {
    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<UserJpaEntity> findByIdWithRoles(@Param("id") String id);

    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserJpaEntity> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.googleSub = :sub")
    Optional<UserJpaEntity> findByGoogleSubWithRoles(@Param("sub") String sub);

    /**
     * Cursor pagination step 1:fetch user ids with id > cursor, ordered by id ASC.
     * Pass {@code Pageable.ofSize(n)} to limit rows.
     * 
     * Keyset pagination - first page (no cursor)
     * Split from the with cursor variant because Postgres can't infer the type of 
     * a null parameter in a {@code :p IS NULL OR u.col < :p} predicate
     * 
     * @param pageable
     * @return
     */
    @Query("SELECT u.id FROM UserJpaEntity u ORDER BY u.createdAt DESC, u.id DESC")
    List<String> findFirstPageIds(Pageable pageable);

    /**
     * Keyset pagination - subsequent page. Tie breaks on id when createdAt equals
     * @param createdAt
     * @param cursor
     * @param pageable
     * @return
     */

    @Query("SELECT u FROM UserJpaEntity u" +
            " WHERE u.createdAt < :createdAt" +
            " OR (u.createdAt  = :createdAt AND u.id < : cursor)" +
            " ORDER BY u.createdAt DESC, u.id DESC")
    List<String> findIdsAfterCursor(
            @Param("createdAt") Instant createdAt,
            @Param("cursor") String cursor,
            Pageable pageable);

    /** Cursor pagination step 2, fetch user with roles  */
    @Query("SELECT DISTINCT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.id IN :ids ORDER BY u.createdAt DESC, u.id DESC")
    List<UserJpaEntity> findAllByIdWithRoles(@Param("ids") List<String> ids);

}
