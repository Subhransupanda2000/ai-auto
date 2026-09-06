package com.healthcareai.ai.tools;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthcareai.exception.BusinessRuleViolationException;

/**
 * Small helper for reading typed values out of the {@link JsonNode}
 * arguments the LLM supplies to a tool call, with clear errors when
 * required arguments are missing or malformed.
 */
final class ToolArguments {

    private ToolArguments() {
    }

    static String requiredString(JsonNode args, String field) {
        JsonNode node = args.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw new BusinessRuleViolationException("Missing required argument: " + field);
        }
        return node.asText();
    }

    static String optionalString(JsonNode args, String field) {
        JsonNode node = args.get(field);
        return (node == null || node.isNull()) ? null : node.asText();
    }

    static UUID requiredUuid(JsonNode args, String field) {
        try {
            return UUID.fromString(requiredString(args, field));
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Argument '" + field + "' must be a valid UUID.");
        }
    }

    static Instant requiredInstant(JsonNode args, String field) {
        try {
            return Instant.parse(requiredString(args, field));
        } catch (Exception e) {
            throw new BusinessRuleViolationException(
                    "Argument '" + field + "' must be an ISO-8601 timestamp, e.g. 2025-01-31T14:30:00Z.");
        }
    }

    static LocalDate requiredDate(JsonNode args, String field) {
        try {
            return LocalDate.parse(requiredString(args, field));
        } catch (Exception e) {
            throw new BusinessRuleViolationException(
                    "Argument '" + field + "' must be an ISO-8601 date, e.g. 2025-01-31.");
        }
    }
}
