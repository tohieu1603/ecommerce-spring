package com.example.auth_service.domain;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.exceptions.TokenReuseDetectedException;
import com.example.auth_service.domain.models.token.vo.RevokedReason;
import com.example.auth_service.domain.models.token.vo.TokenFamily;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.services.TokenDomainService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TokenDomainServiceTest {

    private TokenDomainService service;
    private RefreshTokenRepository repository;

    @BeforeEach
    void setUp() {
        service = new TokenDomainService();
        repository = Mockito.mock(RefreshTokenRepository.class);
    }

    @Nested
    @DisplayName("issueForUser - creating the first refresh token")
    class IssueForUser {

        @Test
        void shouldCreateRootRefreshToken_forNewLogin() {
            User user = Mockito.mock(User.class);
            UserId userId = UserId.of(UUID.randomUUID().toString());
            Mockito.when(user.getId()).thenReturn(userId);

            RefreshToken token = service.issueForUser(user, 7);

            assertThat(token).isNotNull();
            assertThat(token.getUserId()).isEqualTo(userId);
            assertThat(token.getFamily()).isNotNull();
            assertThat(token.getGeneration().value()).isEqualTo(0);
            assertThat(token.isRevoke()).isFalse();
            verify(user).ensureAuthenticatable();
        }
    }

    @Nested
    @DisplayName("Reuse detection — security critical")
    class ReuseDetection {

        @Test
        void shouldThrowEndRevokeFamily_whenFamilyRevokeTokenIsReplayed() {

            User user = Mockito.mock(User.class);
            UserId userId = UserId.of(UUID.randomUUID().toString());
            Mockito.when(user.getId()).thenReturn(userId);

            RefreshToken compromised = service.issueForUser(user, 7);
            compromised.revoke(RevokedReason.FAMILY_REVOKED);

            TokenFamily family = compromised.getFamily();
            Mockito.when(repository.findByFamily(family)).thenReturn(List.of(compromised));

            assertThatThrownBy(() -> service.rotate(compromised, 7, repository))
                    .isInstanceOf(TokenReuseDetectedException.class);

            verify(repository).findByFamily(family);
            assertThat(compromised.isRevoke()).isTrue();
            verify(compromised).revoke(RevokedReason.FAMILY_REVOKED);

        }

        @Test
        void shouldThrowTokenRevoke_whenNormallyRevokedReplayed() {
            User user = Mockito.mock(User.class);
            UserId userId = UserId.of(UUID.randomUUID().toString());
            Mockito.when(user.getId()).thenReturn(userId);

            RefreshToken old = service.issueForUser(user, 7);
            service.rotate(old, 7, repository);

            assertThatThrownBy(() -> service.rotate(old, 7, repository))
                    .isInstanceOf(TokenReuseDetectedException.class);
        }

        @Test
        void shouldRevokeOnlyLiveTokens_whenFamilyRevoke() {
            User user = Mockito.mock(User.class);
            UserId userId = UserId.of(UUID.randomUUID().toString());
            Mockito.when(user.getId()).thenReturn(userId);

            RefreshToken liveToken = service.issueForUser(user, 7);
            RefreshToken alreadyToken = service.issueForUser(user, 7);
            alreadyToken.revoke(RevokedReason.NORMAL);

            Mockito.when(repository.findByFamily(any())).thenReturn(List.of(liveToken, alreadyToken));

            service.revokeFamily(liveToken.getFamily(), repository);

            assertThat(liveToken.isRevoke()).isTrue();
            assertThat(liveToken.getReason()).isEqualTo(RevokedReason.FAMILY_REVOKED);

            verify(repository).save(liveToken);
            verify(repository, Mockito.never()).save(alreadyToken);
        }
    }

    @Nested
    @DisplayName("Token rotation")
    class Rotate {

        @Test
        void shouldRevokeOldAndIssueNext_whenSameFamily() {
            User user = Mockito.mock(User.class);
            UserId userId = UserId.of(UUID.randomUUID().toString());
            Mockito.when(user.getId()).thenReturn(userId);

            RefreshToken old = service.issueForUser(user, 7);
            RefreshToken rotate = service.rotate(old, 7, repository);

            assertThat(old.isRevoke()).isTrue();
            assertThat(old.getReason()).isEqualTo(RevokedReason.NORMAL);
            assertThat(old.getFamily()).isEqualTo(rotate.getFamily());
            assertThat(old.getGeneration().value()).isEqualTo(rotate.getGeneration().value() + 1);
            assertThat(rotate.isRevoke()).isFalse();
            assertThat(rotate.getUserId()).isEqualTo(userId);
            verify(repository).save(old);
            verify(repository).save(rotate);
        }
    }
}
