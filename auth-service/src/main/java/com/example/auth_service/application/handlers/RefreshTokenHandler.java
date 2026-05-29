package com.example.auth_service.application.handlers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.commands.RefreshTokenCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.services.UserPermissionLookupService;
import com.example.auth_service.domain.models.role.Role;
import com.example.auth_service.domain.models.token.RefreshToken;
import com.example.auth_service.domain.models.token.exceptions.TokenRevokedException;
import com.example.auth_service.domain.models.token.vo.TokenValue;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.repositories.RefreshTokenRepository;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;
import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.domain.services.TokenProviderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenHandler implements CommandHandler<RefreshTokenCommand, AuthResponseDTO> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenDomainService tokenDomainService;
    private final TokenProviderPort tokenProviderPort;
    private final UserDtoMapper mapper;
    private final UserPermissionLookupService permissionLookup;

    @Value("${jwt.refresh-expiration-day:7}")
    private int expiryDays;

    @Override
    public AuthResponseDTO handle(RefreshTokenCommand command) {

        RefreshToken presented = refreshTokenRepository.findByTokenValueForUpdate(TokenValue.of(command.refreshToken()))
                .orElseThrow(() -> new TokenRevokedException(null, null));
        
        RefreshToken rotated = tokenDomainService.rotate(presented, expiryDays, refreshTokenRepository);
        refreshTokenRepository.save(rotated);

        User user = userRepository.findById(presented.getUserId())
                .orElseThrow(() -> new UserNotFoundException(presented.getUserId().value()));
        
        List<Role> roles = roleRepository.findByIdIn(user.getRoles());
        Set<String> roleNames = roles.stream()
                .map(r -> r.getName().value())
                .collect(Collectors.toSet());
        Set<String> permissionNames = permissionLookup.getPermissionsForRoles(roles);
        var issued = tokenProviderPort.issueAccessToken(user, roleNames, permissionNames);

        return AuthResponseDTO.bearer(
            issued.token(),
            rotated.getValue().value(),
            issued.expiresInSeconds(),
            mapper.toDto(user, roles));
    }
    
}
