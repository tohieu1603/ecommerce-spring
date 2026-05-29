package com.example.auth_service.application.handlers;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.common.CursorCodec;
import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.dtos.PageDTO;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.application.mapper.UserDtoMapper;
import com.example.auth_service.application.querys.ListUserQuery;
import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.repositories.RoleRepository;
import com.example.auth_service.domain.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly= true)
public class ListUserHandler implements QueryHandler<ListUserQuery, PageDTO<UserDTO>>{

    private static final int LIMIT_MAX = 100;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDtoMapper mapper;

    @Override
    public PageDTO<UserDTO> handle(ListUserQuery query) {
        int pageSize = Math.clamp(query.limit(), 1, LIMIT_MAX);

        CursorCodec.Cursor cursor = CursorCodec.decode(query.cursor());
        Instant cursorCreatedAt = cursor == null ? null : cursor.createdAt();
        String cursorId = cursor == null ? null : cursor.id();

        List<User> rows = userRepository.findAfterCursor(cursorCreatedAt, cursorId, pageSize + 1);
        
        boolean hasNext = rows.size() > pageSize;
        List<User> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

        List<UserDTO> items = pageRows.stream()
            .map(u -> mapper.toDto(u, roleRepository.findByIdIn(u.getRoles())))
            .toList();
        String nextCursor = null;
        if(hasNext && !pageRows.isEmpty()) {
            User lastUser = pageRows.getLast();
            nextCursor = CursorCodec.encode(lastUser.getCreatedAt(), lastUser.getId().value());

        }

        return PageDTO.of(items, nextCursor, pageSize, -1);
    }
    
}
