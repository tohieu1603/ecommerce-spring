package com.example.auth_service.domain.models.user.events;

import com.example.auth_service.domain.events.DomainEvent;

public final class EmailChangeEvent extends DomainEvent{
    
    private final String userId;
    private final String oldEmail;
    private final String newEmail;

    public EmailChangeEvent(String userId, String oldEmail, String newEmail) {
        this.userId = userId;
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    public String userId() {
        return userId;
    }

    public String oldEmail() {
        return oldEmail;
    }

    public String newEmail() {
        return newEmail;
    }
}
