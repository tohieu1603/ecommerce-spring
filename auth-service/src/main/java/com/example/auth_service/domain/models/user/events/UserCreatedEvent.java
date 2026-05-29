package com.example.auth_service.domain.models.user.events;

import com.example.auth_service.domain.events.DomainEvent;

public final class UserCreatedEvent extends DomainEvent{
    private final String userId;
    private final String email;
    private final String username;

    public UserCreatedEvent(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    public String userId() {
        return userId;
    }

    public String email() {
        return email;
    }

    public String username() {
        return username;
    }
}
