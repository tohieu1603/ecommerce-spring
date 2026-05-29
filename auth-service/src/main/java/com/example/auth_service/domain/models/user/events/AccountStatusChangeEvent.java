package com.example.auth_service.domain.models.user.events;

import com.example.auth_service.domain.events.DomainEvent;

public final  class AccountStatusChangeEvent extends DomainEvent{
    
    public enum Transition {
        LOCKED,
        UNLOCKED,
        DISABLED,
        ENABLED
    }

    private final String userId;
    private final String username;
    private final Transition transition;

    public AccountStatusChangeEvent(String userId, String username, Transition transition) {
        this.userId = userId;
        this.username = username;
        this.transition = transition;
    }

    @Override
     public String aggregateId() {
        return userId;
    }

    public String userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public Transition transition() {
        return transition;
    }

    @Override
    public String toString() {
        /** Keep discriminator in the event type for log/kafka consumers that branch on name */
        return "Account " + transition.name().charAt(0)
                + transition.name().substring(1).toLowerCase() + "Event";
    }
}
