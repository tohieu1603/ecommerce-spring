package hieu.com.catalog_service.domain.shared;

public abstract class DomainException extends RuntimeException{
    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public final String code() { return code; }

}
