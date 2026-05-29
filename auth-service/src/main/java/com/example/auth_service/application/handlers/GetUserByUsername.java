package com.example.auth_service.application.handlers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.querys.GetUserByUsernameQuery;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.models.user.vo.Username;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserByUsername implements QueryHandler<GetUserByUsernameQuery, UserDTO>{
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDtoMapper mapper;

    @Override
     public UserDTO handle(GetUserByUsernameQuery query) {
        var user = userRepository.findByUsernameWithRoles(Username.of(query.username()))
                .orElseThrow(() -> new UserNotFoundException(query.username()));
        return mapper.toDto(user, roleRepository.findByIdIn(user.getRoles()));
    }
}
