package com.example.auth_service.domain.models.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.example.auth_service.domain.events.DomainEvent;
import com.example.auth_service.domain.models.role.vo.RoleId;
import com.example.auth_service.domain.models.user.events.AccountStatusChangeEvent;
import com.example.auth_service.domain.models.user.events.EmailChangeEvent;
import com.example.auth_service.domain.models.user.events.OAuthProviderLinkedEvent;
import com.example.auth_service.domain.models.user.events.PasswordChangeEvent;
import com.example.auth_service.domain.models.user.events.RoleAssignedEvent;
import com.example.auth_service.domain.models.user.events.RoleRemoveEvent;
import com.example.auth_service.domain.models.user.events.UserCreatedEvent;
import com.example.auth_service.domain.models.user.exceptions.AccountNotUsableException;
import com.example.auth_service.domain.models.user.exceptions.OAuthAccountAlreadyLinkedException;
import com.example.auth_service.domain.models.user.vo.AccountStatus;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.GoogleSub;
import com.example.auth_service.domain.models.user.vo.Password;
import com.example.auth_service.domain.models.user.vo.PersonName;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.services.PasswordEncodePort;
import com.example.auth_service.domain.shared.AggregateRoot;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * User aggregate root
 * 
 * <p>Encapsulates identity, credentials, account status, role assignments, and token versioning
 * for a user. State transitions go through explicit business methods that enforce invariants
 * and raised {@link DomainEvent} Event mangement is inherited from {@link AggregeteRoot}; events are drained by
 * infrastructure via {@code pullDomainEvents()} after a successful save.
 */

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@ToString(of = {"id", "username", "accountStatus", "email"})
public class User extends AggregateRoot {
    @EqualsAndHashCode.Include
    private UserId id;
    private Username username;
    private AccountStatus accountStatus;
    private Email email;
    private Password password;
    private PersonName personName;
    private Set<RoleId> roles;
    private int tokenVersion;
    private GoogleSub googleSub;
    private Instant createdAt;
    private Instant updatedAt;

    private static List<DomainEvent> events = new ArrayList<>();

    private User() {
        this.roles = new HashSet<>();
    }
    
    public static User register(Username username, Email email, Password password,
                                PersonName personName, PasswordEncodePort encoder) {
        Objects.requireNonNull(encoder, "encoder");
        if(!password.needsEncoding()) {
            throw new IllegalArgumentException("register() requires a raw password");
        }

        User u = new User();
        u.id = UserId.generate();
        u.username = username;
        u.email = email;
        u.password = Password.createEncoded(encoder.encode(password.value()));
        u.personName = personName;
        u.accountStatus = AccountStatus.createActive();
        u.tokenVersion = 1;
        u.createdAt = Instant.now();
        u.updatedAt = Instant.now();

        u.registerEvent(new UserCreatedEvent(u.id.value(), username.value(), email.value()));
        return u;
    }

    public static User reconstitute(
            UserId id,
            Username username,
            Password password,
            Email email,
            PersonName personName,
            AccountStatus accountStatus,
            Set<RoleId> roles,
            int tokenVersion,
            GoogleSub googleSub,
            Instant createdAt,
            Instant updatedAt
    ) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.email = email;
        user.password = password;
        user.personName = personName;
        user.accountStatus = accountStatus;
        user.tokenVersion = tokenVersion;
        user.googleSub = googleSub;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.roles = new HashSet<>(roles);

        return user;
    }

    public static User registerFromGoogle(Username username, Email email, PersonName personName,
                                        GoogleSub googleSub, PasswordEncodePort encoder) {
        Objects.requireNonNull(googleSub, "googleSub");
        Objects.requireNonNull(encoder, "encoder");

        User u = new User();
        u.id = UserId.generate();
        u.username = username;
        u.email = email;
        // Random hash → guaranteed not to match anything a human would type.
        u.password = Password.createEncoded(encoder.encode(UUID.randomUUID().toString()));
        u.personName = personName;
        u.accountStatus = AccountStatus.createActive();
        u.tokenVersion = 1;
        u.googleSub = googleSub;
        u.createdAt = Instant.now();
        u.updatedAt = u.createdAt;

        u.registerEvent(new UserCreatedEvent(u.id.value(), username.value(), email.value()));
        u.registerEvent(new OAuthProviderLinkedEvent(
                u.id.value(), "google", googleSub.value(), /*newAccount*/ true));
        return u;
    }

    public void linkGoogleAccount(GoogleSub sub) {
        Objects.requireNonNull(sub, "sub");

        if(googleSub != null) {
            if(googleSub.equals(sub)) return;
            throw new OAuthAccountAlreadyLinkedException("google");

        }
        googleSub = sub;
        updatedAt = Instant.now();
        registerEvent(new OAuthProviderLinkedEvent(id.value(), "google", sub.value(), false));
    }

    public boolean isLinkedWithGoogle() {
        return googleSub != null;
    }

    public boolean isActive() {
        return accountStatus.isActive();
    }
    public boolean authenticate(Password passwordRaw, PasswordEncodePort encoder) {
       ensureAuthenticatable();
       
       boolean matches = encoder.matches(passwordRaw.value(), password.value());

       if(matches) {
           recordLogin();
       }

       return matches;
    }

    public void recordLogin() {
        this.accountStatus = accountStatus.withLastLogin(Instant.now());
        this.updatedAt = Instant.now();

    }

    public void ensureAuthenticatable() {
        var s = accountStatus;
        if(!s.enabled())                    throw new AccountNotUsableException(AccountNotUsableException.Reason.DISABLED);
        if(!s.accountNonExpired())          throw new AccountNotUsableException(AccountNotUsableException.Reason.EXPIRED);
        if(!s.accountNonLocked())           throw new AccountNotUsableException(AccountNotUsableException.Reason.LOCKED);
        if(!s.credentialsNonExpired())      throw new AccountNotUsableException(AccountNotUsableException.Reason.CREDENTIALS_EXPIRED);
    }

    public void changePassword(Password oldPassword, Password newPassword, PasswordEncodePort encoder) {
        if(!encoder.matches(oldPassword.value(), this.password.value())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        
        password = newPassword.needsEncoding()
                ? Password.createEncoded(encoder.encode(newPassword.value()))
                : newPassword;
        
        increaseTokenVersion();

        registerEvent(new PasswordChangeEvent(id.value(), username.value()));
    }
    public void updateEmail(Email email) {
        if(this.email.equals(email)) {
            return;
        }
        Email oldEmail = this.email;
        this.email = email;
        this.updatedAt = Instant.now();

        registerEvent(new EmailChangeEvent(id.value(), oldEmail.value(), email.value()));

    }
    public void updatePersonName(PersonName personName) {
        if(this.personName.equals(personName)) {
            return;
        }
        PersonName oldPersonName = this.personName;
        this.personName = personName;
        this.updatedAt = Instant.now();

    }
    public void lock() {
        transitionStatus(accountStatus.lock(), AccountStatusChangeEvent.Transition.LOCKED);
    }
    public void unlock() {
        transitionStatus(accountStatus.unlock(), AccountStatusChangeEvent.Transition.UNLOCKED);
    }
    public void disable() {
        transitionStatus(accountStatus.disable(), AccountStatusChangeEvent.Transition.DISABLED);
    }
    public void enable() {
        transitionStatus(accountStatus.enable(), AccountStatusChangeEvent.Transition.ENABLED);
    }

    private void transitionStatus(AccountStatus next, AccountStatusChangeEvent.Transition kind) {
        if(accountStatus.equals(next)) return;

        accountStatus = next;
        updatedAt = Instant.now();

        registerEvent(new AccountStatusChangeEvent(id.value(), username.value(), kind));
    }
    public void assignRoles(RoleId roles) {
        if(this.roles.contains(roles)) {
            return;
        }
        this.roles.add(roles);
        this.updatedAt = Instant.now();

        registerEvent(new RoleAssignedEvent(id.value(), roles.value()));

    }
    public void removeRoles(RoleId roleId) {
        if(!roles.contains(roleId)) {
            return;
        }
        this.roles.remove(roleId);
        this.updatedAt = Instant.now();

        registerEvent(new RoleRemoveEvent(id.value(), roleId.value()));
    }

    public boolean hasRoles(RoleId roleId) {
        return this.roles.contains(roleId);
    }

    public void increaseTokenVersion() {
        this.tokenVersion++;
        this.updatedAt = Instant.now();
    }
}
