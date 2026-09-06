package com.healthcareai.integration;

import java.time.Instant;
import java.util.Optional;

/**
 * Abstraction over the external calendar provider (Google Calendar) used to
 * keep doctor calendars in sync with appointments booked through the AI
 * receptionist. Implementations should be resilient to the integration
 * being disabled/unconfigured (see {@code app.google-calendar.enabled}),
 * returning {@link Optional#empty()} rather than failing the booking flow.
 */
public interface CalendarIntegrationService {

    Optional<String> createEvent(String summary, String description, Instant start, Instant end, String attendeeEmail);

    void cancelEvent(String eventId);

    Optional<String> rescheduleEvent(String eventId, Instant newStart, Instant newEnd);
}
