package hieu.com.catalog_service.domain.events.attribute;

import hieu.com.catalog_service.domain.events.DomainEvent;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class AttrDeletedEvent extends DomainEvent {

    private final String attrId;

    public AttrDeletedEvent(String attrId) {
        this.attrId = Objects.requireNonNull(attrId, "attrId");
    }

    @Override public String aggregateId() { return attrId; }
}
