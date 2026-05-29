package com.example.auth_service.domain.events;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredOn = Instant.now();

    public final UUID eventId() {
        return eventId;
    }

    public final Instant occurredOn() {
        return occurredOn;
    }

    public String eventType() {
        return getClass().getSimpleName();
    }

    public abstract String aggregateId();

    @Override
    public String toString() {
        return "%s{eventId=%s, aggregateId=%s, occurredOn=%s}"
                .formatted(eventType(), eventId, aggregateId(), occurredOn);
    }
}
