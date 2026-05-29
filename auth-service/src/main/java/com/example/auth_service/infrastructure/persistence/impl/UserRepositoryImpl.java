package com.example.auth_service.infrastructure.persistence.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.domain.events.DomainEventPublisher;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.GoogleSub;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.infrastructure.persistence.jpa.entities.RoleJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.entities.UserJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.RoleJpaRepository;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.UserJpaRepository;
import com.example.auth_service.infrastructure.persistence.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * Spring Data JPA adapter for {@link UserRepository}.
 *
 * <p>Bridges the domain aggregate onto its JPA entity via {@link UserMapper}.
 * On every save the adapter drains the aggregate's domain events and forwards them
 * to {@link DomainEventPublisher} — subscribers listening at
 * {@code @TransactionalEventListener(AFTER_COMMIT)} see events only when the surrounding
 * transaction actually commits.
 */

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final RoleJpaRepository roleRepository;
    private final UserJpaRepository userRepository;
    private final UserMapper mapper;
    private final DomainEventPublisher event;


    @Override
    @Transactional
    public User save(User user) {
        boolean isNew = !userRepository.existsById(user.getId().value());

        Set<String> roleIds = user.getRoles().stream()
                .map(RoleId::value)
                .collect(Collectors.toSet());
        Set<RoleJpaEntity> roleJpaEntitys = roleRepository.findByIdIn(roleIds);

        UserJpaEntity saved = userRepository.save(mapper.toEntity(user, roleJpaEntitys, isNew));

        user.pullDomainEvents().forEach(event::publish);

        return mapper.toDomain(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId userId) {
        return userRepository.findById(userId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(Username username) {
        return userRepository.findByUsername(username.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        return userRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return userRepository.existsByUsername(username.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userRepository.existsByEmail(email.value());
    }

    @Override
    public void delete(User user) {
        if(user.getId() != null) {
            userRepository.deleteById(user.getId().value());
        }
    }

    @Override
    public Optional<User> findByIdWithRoles(UserId userId) {
        return userRepository.findByIdWithRoles(userId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameWithRoles(Username username) {
        return userRepository.findByUsernameWithRoles(username.value())
                .map(mapper::toDomain);
    }



    @Override
    public List<User> findAfterCursor(Instant createdAt, String cursor, int limit) {
        var pageable = PageRequest.of(0, limit);

        List<String> ids = (createdAt== null)
                ? userRepository.findFirstPageIds(pageable)
                : userRepository.findIdsAfterCursor(createdAt, cursor, pageable);
        if(ids.isEmpty()) return Collections.emptyList();

        return userRepository.findAllByIdWithRoles(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByGoogleSub(GoogleSub sub) {
        return userRepository.findByGoogleSubWithRoles(sub.value()).map(mapper::toDomain);
    }
}
