package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.LoginCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.services.UserPermissionLookupService;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.InvalidCredentialsException;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.Password;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.domain.services.PasswordEncodePort;
import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.domain.services.TokenProviderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class LoginHandler implements CommandHandler<LoginCommand, AuthResponseDTO>{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncodePort passwordEncodePort;
    private final TokenDomainService tokenDomainService;
    private final UserDtoMapper mapper;
    private final UserPermissionLookupService permissionLookup;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshTokenExpiryDays;

    @Override
    public AuthResponseDTO handle(LoginCommand command) {
        User user = lookup(command.usernameOrEmail())
            .orElseThrow(InvalidCredentialsException::new);
        
        boolean ok = user.authenticate(Password.createRaw(command.password()), passwordEncodePort);

        if(!ok) {
            throw new InvalidCredentialsException();
        }
        User saved = userRepository.save(user);
        List<Role> roles = roleRepository.findByIdIn(saved.getRoles());

        Set<String> roleNames = roles.stream()
            .map(r -> r.getName().value())
            .collect(Collectors.toSet());
        Set<String> permissionNames = permissionLookup.getPermissionsForRoles(roles);

        var issued = tokenProviderPort.issueAccessToken(saved, roleNames, permissionNames);
        RefreshToken refresh = tokenDomainService.issueForUser(user, refreshTokenExpiryDays);
        refreshTokenRepository.save(refresh);

        return AuthResponseDTO.bearer(
            issued.token(),
            refresh.getValue().value(),
            issued.expiresInSeconds(),
            mapper.toDto(saved, roles)
        );

    }
    
    private Optional<User> lookup(String userNameOrEmail) {
        if(userNameOrEmail == null || userNameOrEmail.isBlank()) return Optional.empty();
        
        try {
            return userNameOrEmail.contains("@")
                ? userRepository.findByEmail(Email.of(userNameOrEmail))
                : userRepository.findByUsername(Username.of(userNameOrEmail));

        } catch (Exception e) {
            log.error("Error occurred while looking up user: {}", userNameOrEmail, e);
            return Optional.empty();
        }
    }
}
