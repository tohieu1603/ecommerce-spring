package hieu.com.catalog_service.domain.model.attribute.vo;

import java.util.Objects;
import java.util.UUID;

public record AttrId(String value) {
    public AttrId {
        Objects.requireNonNull(value, "Attribute id cannot be null");
        if(value.isBlank()) {
            throw new IllegalArgumentException("AttrId cannot be blank");
        }
    }
    
    public static AttrId of(String value) {
        return new AttrId(value);
    }

        public static AttrId generate() {
        return new AttrId(UUID.randomUUID().toString());
    }
}
