package com.example.auth_service.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.auth_service.domain.events.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Bridges in-process {@link DomainEvent}s onto Kafka integration events.
 *
 * <p>Runs on {@link TransactionPhase#AFTER_COMMIT} so Kafka only sees events for
 * transactions that actually committed — prevents the classic "event published but
 * DB write rolled back" bug.
 *
 * <p>Publishing is best-effort at this layer: failures are logged but not retried.
 * Upgrade to the Transactional Outbox pattern if at-least-once delivery becomes a
 * hard requirement (the outbox table lives in the same transaction, a separate
 * poller ships rows to Kafka with retries).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaIntegrationEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DomainEventToIntegrationEventMapper mapper;

    /**
     * Listens to every {@link DomainEvent} published via
     * {@link com.hieu.auth_service.domain.events.DomainEventPublisher} and routes it
     * to Kafka.
     *
     * @param event raw domain event from an aggregate
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        var routed = mapper.map(event);
        if(routed == null) return;

        try {
            kafkaTemplate.send(routed.topic(), routed.key(), routed.event());
            log.debug("Published {} to {} (key = {})", routed.event(), routed.topic(), routed.key());
        } catch (Exception e) {
            log.error("Failed to publish event {}", routed.event(), e);
        }
    }
}
