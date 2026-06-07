package hieu.com.catalog_service.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hieu.com.catalog_service.domain.events.DomainEvent;

public abstract class AggregateRoot {
    private final transient List<DomainEvent> events = new ArrayList<>();

    protected final void registerEvent(DomainEvent events) {
        if(events != null) this.events.add(events);
    }

    public final List<DomainEvent> pullDomainEvent() {
        if(events.isEmpty()) return List.of();
        List<DomainEvent> snapshot = List.copyOf(events);
        events.clear();
        return snapshot;
    }

    public final List<DomainEvent> peekDomainEvent() {
        return Collections.unmodifiableList(events);
    }

    protected final void clearDomainEvent() {
        events.clear();
    }
}
