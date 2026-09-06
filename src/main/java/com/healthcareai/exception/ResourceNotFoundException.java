package com.healthcareai.exception;

/**
 * Thrown when a requested entity (patient, doctor, appointment, ...) cannot
 * be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityType, Object id) {
        return new ResourceNotFoundException(entityType + " not found: " + id);
    }
}
