package com.dom.common.exception;

/**
 * Thrown when a downstream service is unavailable (circuit breaker open).
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String service, Throwable cause) {
        super(String.format("Service '%s' is currently unavailable. Please try again later.", service), cause);
    }
}
