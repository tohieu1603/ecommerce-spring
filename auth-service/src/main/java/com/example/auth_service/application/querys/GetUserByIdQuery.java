

package com.example.auth_service.application.querys;

import com.example.auth_service.application.common.Query;
import com.example.auth_service.application.dtos.UserDTO;

public record GetUserByIdQuery(String userId) implements Query<UserDTO>{}
