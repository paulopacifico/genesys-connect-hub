package com.genesyshub.domain.model;

public class DomainException extends RuntimeException {

    public enum ErrorCode {
        GENESYS_AUTH_FAILED,
        GENESYS_API_ERROR,
        QUEUE_NOT_FOUND,
        AGENT_NOT_FOUND,
        INVALID_WEBHOOK_SIGNATURE,
        RATE_LIMIT_EXCEEDED
    }

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public DomainException(ErrorCode code, String message) {
        this(code, message, null);
    }

    public ErrorCode getCode() {
        return code;
    }
}
