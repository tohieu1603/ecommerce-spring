package hieu.com.catalog_service.domain.events.product;

import hieu.com.catalog_service.domain.events.DomainEvent;
import hieu.com.catalog_service.domain.model.product.vo.ProductStatus;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class ProductStatusChangedEvent extends DomainEvent {

    private final String productId;
    private final ProductStatus oldStatus;
    private final ProductStatus newStatus;
    private final String updatedBy;

    public ProductStatusChangedEvent(String productId, ProductStatus oldStatus,
                                      ProductStatus newStatus, String updatedBy) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.oldStatus = Objects.requireNonNull(oldStatus, "oldStatus");
        this.newStatus = Objects.requireNonNull(newStatus, "newStatus");
        this.updatedBy = updatedBy;
    }

    @Override public String aggregateId() { return productId; }
}
