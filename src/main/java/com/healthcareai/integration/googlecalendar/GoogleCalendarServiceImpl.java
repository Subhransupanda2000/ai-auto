package com.healthcareai.integration.googlecalendar;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.healthcareai.config.GoogleCalendarProperties;
import com.healthcareai.integration.CalendarIntegrationService;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link CalendarIntegrationService} implementation backed by the Google
 * Calendar API, authenticated via a service account key file. Calendar
 * sync is best-effort: if the integration is disabled or misconfigured,
 * calls return {@link Optional#empty()} instead of throwing, so the core
 * appointment booking flow is never blocked by a calendar outage.
 */
@Component
@Slf4j
public class GoogleCalendarServiceImpl implements CalendarIntegrationService {

    private final GoogleCalendarProperties properties;
    private volatile Calendar calendarClient;

    public GoogleCalendarServiceImpl(GoogleCalendarProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> createEvent(String summary, String description, Instant start, Instant end, String attendeeEmail) {
        return withCalendar(calendar -> {
            Event event = new Event()
                    .setSummary(summary)
                    .setDescription(description)
                    .setStart(toEventDateTime(start))
                    .setEnd(toEventDateTime(end));
            if (attendeeEmail != null && !attendeeEmail.isBlank()) {
                event.setAttendees(List.of(new EventAttendee().setEmail(attendeeEmail)));
            }
            Event created = calendar.events().insert(properties.calendarId(), event).execute();
            return created.getId();
        });
    }

    @Override
    public void cancelEvent(String eventId) {
        withCalendar(calendar -> {
            calendar.events().delete(properties.calendarId(), eventId).execute();
            return null;
        });
    }

    @Override
    public Optional<String> rescheduleEvent(String eventId, Instant newStart, Instant newEnd) {
        return withCalendar(calendar -> {
            Event event = calendar.events().get(properties.calendarId(), eventId).execute();
            event.setStart(toEventDateTime(newStart));
            event.setEnd(toEventDateTime(newEnd));
            Event updated = calendar.events().update(properties.calendarId(), eventId, event).execute();
            return updated.getId();
        });
    }

    private EventDateTime toEventDateTime(Instant instant) {
        return new EventDateTime().setDateTime(new DateTime(instant.toEpochMilli()));
    }

    private Optional<String> withCalendar(CalendarAction action) {
        if (!properties.enabled()) {
            log.debug("Google Calendar integration disabled; skipping calendar sync.");
            return Optional.empty();
        }
        try {
            Calendar calendar = getOrCreateClient();
            return Optional.ofNullable(action.apply(calendar));
        } catch (Exception e) {
            log.warn("Google Calendar API call failed; continuing without calendar sync.", e);
            return Optional.empty();
        }
    }

    private Calendar getOrCreateClient() throws GeneralSecurityException, IOException {
        Calendar client = calendarClient;
        if (client == null) {
            synchronized (this) {
                client = calendarClient;
                if (client == null) {
                    client = buildClient();
                    calendarClient = client;
                }
            }
        }
        return client;
    }

    private Calendar buildClient() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials;
        try (FileInputStream keyStream = new FileInputStream(properties.serviceAccountKeyPath())) {
            credentials = GoogleCredentials.fromStream(keyStream)
                    .createScoped(List.of(CalendarScopes.CALENDAR));
        }
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName(properties.applicationName())
                .build();
    }

    @FunctionalInterface
    private interface CalendarAction {
        String apply(Calendar calendar) throws IOException;
    }
}
