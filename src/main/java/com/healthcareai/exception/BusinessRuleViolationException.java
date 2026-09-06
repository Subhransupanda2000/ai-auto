package com.healthcareai.exception;

/**
 * Thrown when an operation would violate a domain/business rule (e.g.
 * cancelling an already-completed appointment, booking outside clinic
 * working hours).
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
