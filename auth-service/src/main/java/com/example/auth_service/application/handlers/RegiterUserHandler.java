package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.RegisterUserCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.services.UserPermissionLookupService;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.role.vo.RoleName;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.UserAlreadyExistsException;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.Password;
import com.example.auth_service.domain.models.user.vo.PersonName;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.domain.services.PasswordEncodePort;
import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.domain.services.TokenProviderPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RegiterUserHandler implements CommandHandler<RegisterUserCommand, AuthResponseDTO>{

    private static final String DEFAULT_ROLE = "ROLE_USER";
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncodePort passwordEncodePort;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenDomainService tokenDomainService;
    private final TokenProviderPort tokenProviderPort;
    private final UserDtoMapper mapper;
    private final UserPermissionLookupService permissionLookup;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshExpiryDays;

    @Override
    public AuthResponseDTO handle(RegisterUserCommand command) {
        Username username = Username.of(command.username());
        Email email = Email.of(command.email());

        if(userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException(command.username());
        }
        if(userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email" + command.email());
        }

        User user = User.register(
            username,
            email,
            Password.createRaw(command.rawPassword()),
            PersonName.of(command.firstName(), command.lastName()),
            passwordEncodePort
        );

        roleRepository.findByName(RoleName.of(DEFAULT_ROLE))
                .ifPresent(r -> user.assignRoles(r.getId()));
        
        User saved = userRepository.save(user);
        List<Role> roles = roleRepository.findByIdIn(saved.getRoles());
        Set<String> roleNames = roles.stream()
            .map(r -> r.getName().value())
            .collect(Collectors.toSet());
        Set<String> permissionNames = permissionLookup.getPermissionsForRoles(roles);
        var issued = tokenProviderPort.issueAccessToken(saved, roleNames, permissionNames);

        RefreshToken refresh = tokenDomainService.issueForUser(saved, refreshExpiryDays);
        refreshTokenRepository.save(refresh);

        return AuthResponseDTO.bearer(
            issued.token(),
            refresh.getValue().value(),
            issued.expiresInSeconds(),
            mapper.toDto(user, roles));
    }
    
}
