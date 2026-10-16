package com.acme.salary.common;

/** The request is well-formed but violates a business rule. Mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
