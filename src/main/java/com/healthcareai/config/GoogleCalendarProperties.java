package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Google Calendar integration (service account based).
 */
@ConfigurationProperties(prefix = "app.google-calendar")
public record GoogleCalendarProperties(
        String applicationName,
        String calendarId,
        String serviceAccountKeyPath,
        boolean enabled
) {
}
