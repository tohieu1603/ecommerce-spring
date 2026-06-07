package hieu.com.catalog_service.domain.exception;

import hieu.com.catalog_service.domain.shared.DomainException;
import com.hieu.common.error.ErrorCode;

public final class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND.code(), "Product not found: " + productId);
    }

    public ProductNotFoundException(String criterion) {
        super(ErrorCode.PRODUCT_NOT_FOUND.code(), "Product not found: " + criterion);
    }
}
