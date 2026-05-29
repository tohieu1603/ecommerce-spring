package com.example.auth_service.application.querys;

import com.example.auth_service.application.common.Query;
import com.example.auth_service.application.dtos.PageDTO;
import com.example.auth_service.application.dtos.UserDTO;

public record ListUserQuery(
    String cursor,
    int limit
) implements Query<PageDTO<UserDTO>>{
    
}
