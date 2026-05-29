package com.example.auth_service.application.querys;

import com.example.auth_service.application.common.Query;

public record CheckRoleQuery(String userId, String roleName) implements Query<Boolean> {}
