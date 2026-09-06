package com.healthcareai.dto;

import java.time.Instant;

/**
 * A free appointment slot for a given doctor.
 */
public record AvailableSlot(Instant start, Instant end) {
}
