package com.example.auth_service.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.auth_service.domain.events.DomainEvent;

/**
 * Base class for all aggregate roots
 * 
 * <p>Centralises domain event bookkeeping so individual aggregates can focus on
 * business invariants. Infrastructure drains events via {@link pullDomainEvents()}
 * after a successful transaction commit - a single drain semantics avoids the
 * "forgot to clearEvents" bug plagues hand-rolled ArrayList
 */

public abstract  class AggregateRoot {
    private final transient List<DomainEvent> events = new ArrayList<>();


    /** Drains domain events registered on this aggregate since last drain. */    
    protected final void registerEvent(DomainEvent event) {
        if(event == null) return;
        events.add(event);
    }
    
    /**
     * Automatically returns a snapshot of pending events and clears the internal buffer.
     * Internal to be callerd exactly once per unit-of-work after a successful transaction commit.
     * Idempotent - if there are no pending events, returns an empty list.
     * save() <em>after</em> calling this method will not cause the returned events to be re-published, as the internal buffer is cleared.
     * @return
     */
    public final List<DomainEvent> pullDomainEvents() {
        if (events.isEmpty()) return List.of();
        List<DomainEvent> snapshot = List.copyOf(events);
        events.clear();
        return snapshot;
    }
    
    /**
     * Returns an unmodifiable view of pending events. 
     * Mainly for testing and debugging - use with care in production code as it 
     * does not clear the internal buffer and may cause events to be published multiple times if save() is called after peeking.
     * @return
     */
    public final List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Clears pending events without returning them. Mainly for testing - use with care in production 
     * code as it may cause events to be lost if save() is called after clearing.
     */
    protected final void clearDomainEvents() {
        events.clear();
    }
}
