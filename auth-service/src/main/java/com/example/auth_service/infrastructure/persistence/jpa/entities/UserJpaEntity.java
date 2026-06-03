package com.example.auth_service.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserJpaEntity extends BaseManualIdEntity{

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "google_sub", length = 255)
    private String googleSub;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Builder.Default
    @Column(name = "token_versions", nullable = false)
    private Integer tokenVersions = 1;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleJpaEntity> roles = new HashSet<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    public UserJpaEntity(String id, String username, String email, String password,
                         String firstName, String lastName, boolean enabled,
                         boolean accountNonExpired, boolean accountNonLocked,
                         boolean credentialsNonExpired, Instant lastLogin,
                         Integer tokenVersion, String googleSub, Set<RoleJpaEntity> roles,
                         Instant createdAt, Instant updatedAt, boolean isNew) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.lastLogin = lastLogin;
        this.tokenVersions = tokenVersion;
        this.googleSub = googleSub;
        this.roles = roles != null ? roles : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isNew = isNew; 
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
