package com.example.auth_service.domain.models.user.events;

import com.example.auth_service.domain.events.DomainEvent;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OAuthProviderLinkedEvent extends DomainEvent{
    private final String userId;
    private final String provider;
    private final String providerUserId;
    private final boolean newAccount;

    @Override
    public String aggregateId() {
        return userId;
    }

    public String userId()         { return userId; }
    public String provider()       { return provider; }
    public String providerUserId() { return providerUserId; }
    public boolean newAccount()    { return newAccount; }

}
