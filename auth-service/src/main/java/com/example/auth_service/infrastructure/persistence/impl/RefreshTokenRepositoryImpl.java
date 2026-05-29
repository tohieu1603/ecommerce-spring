package com.example.auth_service.infrastructure.persistence.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.domain.events.DomainEventPublisher;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.vo.TokenFamily;
import com.example.auth_service.domain.models.token.vo.TokenId;
import com.example.auth_service.domain.models.token.vo.TokenValue;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.infrastructure.persistence.jpa.entities.RefreshTokenJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.entities.UserJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.RefreshTokenJpaRepository;
import com.example.auth_service.infrastructure.persistence.jpa.repositories.UserJpaRepository;
import com.example.auth_service.infrastructure.persistence.mapper.RefreshTokenJpaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final RefreshTokenJpaMapper mapper;
    private final DomainEventPublisher event;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        UserJpaEntity useRef = userRepository.findById(token.getUserId().value())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot save refresh token - owning user not found " + token.getUserId().value()));
        
        boolean isNew = !repository.existsById(token.getId().value());
        RefreshTokenJpaEntity saved = repository.save(mapper.toJpaEntity(token, useRef, isNew));
        
        token.pullDomainEvents().forEach(event::publish);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findById(TokenId tokenId) {
        return repository.findById(tokenId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenValue(TokenValue tokenValue) {
        return repository.findByToken(tokenValue.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByUserId(UserId userId) {
        return repository.findByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findValidTokensByUserId(UserId userId) {
        return repository.findValidTokenByUserId(userId.value(), Instant.now()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByFamily(TokenFamily family) {
        return repository.findByFamily(family.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(RefreshToken token) {
        if(token.getId() != null) {
            repository.deleteById(token.getId().value());
        }
    }

    @Override
    @Transactional
    public void deleteByUserId(UserId userId) {
        repository.deleteByUserId(userId.value());
    }

    @Override
    @Transactional
    public int deleteExpiredTokens() {
        return repository.deleteExpriedToken(Instant.now());
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(UserId userId) {
        repository.revokeAllTokenForUser(userId.value(), Instant.now());
    }

    @Override
    public Optional<RefreshToken> findByTokenValueForUpdate(TokenValue tokenValue) {
        return repository.findByTokenForUpdate(tokenValue.value())
                .map(mapper::toDomain);
    }
}
