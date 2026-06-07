package hieu.com.catalog_service.domain.exception;

import com.hieu.common.error.ErrorCode;

import hieu.com.catalog_service.domain.shared.DomainException;

public final class AttrNotFoundException extends DomainException {
    public AttrNotFoundException(Long attrId) {
        super(ErrorCode.ATTR_NOT_FOUND.code(), "Attribute not found: " + attrId);
    }
    public AttrNotFoundException(String code) {
        super(ErrorCode.ATTR_NOT_FOUND.code(), "Attribute not found: " + code);
    }
}
