package hieu.com.catalog_service.domain.model.attribute.vo;

/**
 * Data type of a catalog attribute.
 *
 * <p>NOTE: scaffolded values — adjust to match the catalog domain rules.
 */
public enum AttrType {
    SELECT,
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    MULTI_SELECT;

    public static AttrType fromString(String type) {
        if(type == null) return SELECT;
        return switch (type.toUpperCase()) {
            case "TEXT" -> TEXT;
            case "NUMBER" -> NUMBER;
            case "BOOLEAN" -> BOOLEAN;
            case "DATE" -> DATE;
            case "MULTI_SELECT" -> MULTI_SELECT;
            default -> SELECT;
        };
    }
    public boolean allowsFreeText() {
        return this == TEXT || this == NUMBER;
    }
}
