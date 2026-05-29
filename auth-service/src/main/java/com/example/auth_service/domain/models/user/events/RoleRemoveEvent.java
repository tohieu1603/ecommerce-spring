package com.example.auth_service.domain.models.user.events;

import com.example.auth_service.domain.events.DomainEvent;

public final class RoleRemoveEvent extends DomainEvent{
    private final String userId;
    private final String roleId;

    public RoleRemoveEvent(String userId, String roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    public String userId() {
        return userId;
    }

    public String roleId() {
        return roleId;
    }
}
