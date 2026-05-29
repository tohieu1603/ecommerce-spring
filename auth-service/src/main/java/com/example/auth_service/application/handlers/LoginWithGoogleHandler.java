package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.LoginWithGoogleCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.services.UserPermissionLookupService;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.OAuthEmailNotVerifiedException;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.GoogleSub;
import com.example.auth_service.domain.models.user.vo.PersonName;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.domain.services.GoogleIdTokenVerifierPort;
import com.example.auth_service.domain.services.GoogleIdTokenVerifierPort.GoogleClaims;
import com.example.auth_service.domain.services.PasswordEncodePort;
import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.domain.services.TokenProviderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoginWithGoogleHandler implements CommandHandler<LoginWithGoogleCommand, AuthResponseDTO>{

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";
    private static final int MAX_USERNAME_SUFFIX_TRIES = 1000;

    private final GoogleIdTokenVerifierPort googleVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProviderPort tokenProviderPort;
    private final TokenDomainService tokenDomainService;
    private final PasswordEncodePort passwordEncodePort;
    private final UserDtoMapper mapper;
    private final UserPermissionLookupService permissionLookup;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshExpiryDays = 7;

    @Override
    public AuthResponseDTO handle(LoginWithGoogleCommand command) {
        GoogleClaims claims = googleVerifier.verify(command.idToken());
        if(!claims.emailVerified()) {
            throw new OAuthEmailNotVerifiedException();
        }

        User user = userRepository.findByGoogleSub(GoogleSub.of(claims.sub()))
                .or(() -> userRepository.findByEmail(Email.of(claims.email())))
                .map(found -> {
                    found.linkGoogleAccount(GoogleSub.of(claims.sub()));
                    return found;
                })
                .orElseGet(() -> registerNewGoogleUser(claims));
        
        user.ensureAuthenticatable();

        User saved = userRepository.save(user);
        List<Role> roles = roleRepository.findByIdIn(saved.getRoles());
        Set<String> roleNames = roles.stream()
                .map(r -> r.getName().value())
                .collect(Collectors.toSet());
        
        Set<String> permissionNames = permissionLookup.getPermissionsForRoles(roles);

        var issued = tokenProviderPort.issueAccessToken(user, roleNames, permissionNames);
        RefreshToken refresh = tokenDomainService.issueForUser(user, refreshExpiryDays);
        refreshTokenRepository.save(refresh);

        log.info("Login Google success userId{}, sub{}", saved.getId().value(), claims.sub());
        
        return AuthResponseDTO.bearer(
            issued.token(),
            refresh.getValue().value(),
            issued.expiresInSeconds(),
            mapper.toDto(saved, roles));
    }

    private User registerNewGoogleUser(GoogleClaims claims) {
        Username username = deriveUniqueUsername(claims);
        PersonName personName = derivePersonName(claims);
        Email email = Email.of(claims.email());
        GoogleSub sub = GoogleSub.of(claims.sub());

        User user = User.registerFromGoogle(username, email, personName, sub, passwordEncodePort);

        roleRepository.findByName(RoleName.of(DEFAULT_ROLE))
                .ifPresent(role -> user.assignRoles(role.getId()));

        return user;
    }

private Username deriveUniqueUsername(GoogleClaims claims) {
        String base = sanitiseUsername(claims.email());
        if (!userRepository.isUsernameTaken(safeUsername(base))) {
            return Username.of(base);
        }
        for (int i = 1; i < MAX_USERNAME_SUFFIX_TRIES; i++) {
            String candidate = base + i;
            if (!userRepository.isUsernameTaken(safeUsername(candidate))) {
                return Username.of(candidate);
            }
        }
        // Extremely improbable in practice; fall back to a uuid-suffixed name.
        return Username.of(base + "_" + java.util.UUID.randomUUID().toString().substring(0, 8));
    }
    
     private static String sanitiseUsername(String email) {
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String cleaned = local.replaceAll("[^a-zA-Z0-9_-]", "");
        if (cleaned.length() < 3) cleaned = cleaned + "user";
        if (cleaned.length() > 50) cleaned = cleaned.substring(0, 50);
        return cleaned;
    }

    private static Username safeUsername(String candidate) {
        try {
            return Username.of(candidate);
        } catch (Exception e) {
            return Username.of(UUID.randomUUID().toString().substring(0, 8) + "x");
        }
    }
    private static PersonName derivePersonName(GoogleClaims claims) {
        String given = blankToDefault(claims.givenName(), "Google");
        String family = blankToDefault(claims.familyName(), "User");
        // If name is provided as a single string and given/family are blank,
        // split on whitespace once.
        if ((isBlank(claims.givenName()) || isBlank(claims.familyName()))
                && !isBlank(claims.name()) && claims.name().contains(" ")) {
            String[] parts = claims.name().trim().split("\\s+", 2);
            given = isBlank(claims.givenName())  ? parts[0] : given;
            family = isBlank(claims.familyName()) ? parts[1] : family;
        }
        return PersonName.of(given, family);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String blankToDefault(String s, String fallback) {
        return isBlank(s) ? fallback : s.trim();
    }
}
