package com.example.auth_service.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.auth_service.domain.events.DomainEvent;
import com.example.auth_service.domain.events.DomainEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(DomainEvent event) {
        if(event == null) return;
        log.debug("Publishing {} [eventId= {}, aggregateId={}]",
            event.eventType(), event.eventId(), event.aggregateId());
        delegate.publishEvent(event);
    }
}
