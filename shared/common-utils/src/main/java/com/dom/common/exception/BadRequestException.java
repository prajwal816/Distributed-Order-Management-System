package com.dom.common.exception;

/**
 * Thrown when a client request is malformed or contains invalid data.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
