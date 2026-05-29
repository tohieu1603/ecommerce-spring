package com.example.auth_service.application.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SessionIntegationEvent(
        UUID eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        int schemaVersion,
        Map<String, Object> payload
)implements IntegrationEvent {}
