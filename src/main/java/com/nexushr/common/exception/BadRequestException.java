package com.nexushr.common.exception;

/** Thrown on invalid client input / business-rule violations. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
