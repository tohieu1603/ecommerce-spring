package com.example.auth_service.application.querys;

import com.example.auth_service.application.common.Query;

public record CheckPermissionQuery(String userId, String permissionName) implements Query<Boolean> {
    
}
