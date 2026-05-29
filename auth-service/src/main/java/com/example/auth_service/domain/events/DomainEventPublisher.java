package com.example.auth_service.domain.events;

/**
 * Outbound port - domain publishes events without knowing about any broker/framework
 */

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    /**
     * Accepts any iterable of events (or subtype). Implementations may batch 
     * @param events
     */
    default void publishAll(Iterable<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
