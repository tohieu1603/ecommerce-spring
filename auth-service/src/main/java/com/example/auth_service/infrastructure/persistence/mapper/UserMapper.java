package com.example.auth_service.infrastructure.persistence.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.AccountStatus;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.GoogleSub;
import com.example.auth_service.domain.models.user.vo.Password;
import com.example.auth_service.domain.models.user.vo.PersonName;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.infrastructure.persistence.jpa.entities.RoleJpaEntity;
import com.example.auth_service.infrastructure.persistence.jpa.entities.UserJpaEntity;

/**
 * Anti-Corruption Layer between the {@code User} onto a persistable JPA entity.
 * 
 * <p>All translation lives here so the domain class can remain framework-free and the JPA entity
 * can evolve independently
 */
@Component
public class UserMapper {

    /** 
     * Project a domain {@link User} onto persistable {@link UserJpaEntity}
     * 
     * @param user doamin aggregate
     * @param roles Jpa role entities already loaded in the current persistence context
     * @param isNew true -> INSERT path; false -> UPDATE path (used by Persistable contract)
     * @return populated JPA entity
     * */
    public UserJpaEntity toEntity(User user, Set<RoleJpaEntity> roles, boolean isNew) {
        if(user == null) {
            return null;
        }
        var s = user.getAccountStatus();
        return UserJpaEntity.builder()
                .id(user.getId() != null ? user.getId().value(): null)
                .username(user.getUsername().value())
                .password(user.getPassword().value())
                .email(user.getEmail().value())
                .firstName(user.getPersonName().firstName())
                .lastName(user.getPersonName().lastName())
                .enabled(s.enabled())
                .accountNonExpired(user.getAccountStatus().accountNonExpired())
                .accountNonLocked(user.getAccountStatus().accountNonLocked())
                .credentialsNonExpired(user.getAccountStatus().credentialsNonExpired())
                .lastLogin(user.getAccountStatus().lastLogin())
                .tokenVersions(user.getTokenVersion())
                .googleSub(user.getGoogleSub() != null ? user.getGoogleSub().value() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }
    public User toDomain(UserJpaEntity user) {
        if(user == null) {
            return null;
        }
        Set<RoleId> roles = user.getRoles().stream()
                .map(role -> RoleId.of(role.getId()))
                .collect(Collectors.toSet());

        return User.reconstitute(
                UserId.of(user.getId()),
                Username.of(user.getUsername()),
                Password.createEncoded(user.getPassword()),
                Email.of(user.getEmail()),
                PersonName.of(user.getFirstName(), user.getLastName()),
                AccountStatus.of(
                        user.isEnabled(),
                        user.isAccountNonExpired(),
                        user.isCredentialsNonExpired(),
                        user.isAccountNonLocked(),
                        user.getLastLogin()
                ),
                roles,
                user.getTokenVersions() != null ? user.getTokenVersions() : 1,
                user.getGoogleSub() != null ? GoogleSub.of(user.getGoogleSub()) : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
    public void updateUserJpaEntity(User user, UserJpaEntity entity, Set<RoleJpaEntity> roles) {
        entity.setUsername(user.getUsername().value());
        entity.setEmail(user.getEmail().value());
        entity.setPassword(user.getPassword().value());
        entity.setFirstName(user.getPersonName().firstName());
        entity.setLastName(user.getPersonName().lastName());
        entity.setEnabled(user.getAccountStatus().enabled());
        entity.setAccountNonExpired(user.getAccountStatus().accountNonExpired());
        entity.setAccountNonLocked(user.getAccountStatus().accountNonLocked());
        entity.setCredentialsNonExpired(user.getAccountStatus().credentialsNonExpired());
        entity.setLastLogin(user.getAccountStatus().lastLogin());
        entity.setTokenVersions(user.getTokenVersion());
        entity.setGoogleSub(user.getGoogleSub() != null ? user.getGoogleSub().value() : null);
        entity.setRoles(roles);
        entity.setUpdatedAt(user.getUpdatedAt());    }
}
