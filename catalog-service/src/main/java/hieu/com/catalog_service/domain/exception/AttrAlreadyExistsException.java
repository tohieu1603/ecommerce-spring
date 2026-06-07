package hieu.com.catalog_service.domain.exception;

import com.hieu.common.error.ErrorCode;

import hieu.com.catalog_service.domain.shared.DomainException;

public final class AttrAlreadyExistsException extends DomainException {
    public AttrAlreadyExistsException(String code) {
        super(ErrorCode.ATTR_ALREADY_EXISTS.code(), "Attribute code already exists: " + code);
    }
}
