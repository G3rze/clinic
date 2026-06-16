package com.terraplanistas.clinic.http.google;

import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleCalendarService extends GoogleApiService {

    public GoogleCalendarService(GoogleTokenService tokenService) {
        super(tokenService);
    }

    public String listCalendars(String googleUserId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            var calendars = calendar.calendarList().list().execute();
            return calendars.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to list calendars: " + e.getMessage(), e);
        }
    }

    public String getCalendar(String googleUserId, String calendarId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            var cal = calendar.calendars().get(calendarId).execute();
            return cal.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to get calendar: " + e.getMessage(), e);
        }
    }

    public String createCalendar(String googleUserId, String summary, String description) {
        try {
            var calendar = createCalendarClient(googleUserId);
            var calendarEntry = new com.google.api.services.calendar.model.Calendar()
                    .setSummary(summary)
                    .setDescription(description);
            var result = calendar.calendars().insert(calendarEntry).execute();
            return result.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to create calendar: " + e.getMessage(), e);
        }
    }

    public String updateCalendar(String googleUserId, String calendarId, String summary, String description) {
        try {
            var calendar = createCalendarClient(googleUserId);
            var calendarEntry = new com.google.api.services.calendar.model.Calendar()
                    .setSummary(summary)
                    .setDescription(description);
            var result = calendar.calendars().patch(calendarId, calendarEntry).execute();
            return result.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to update calendar: " + e.getMessage(), e);
        }
    }

    public void deleteCalendar(String googleUserId, String calendarId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            calendar.calendars().delete(calendarId).execute();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to delete calendar: " + e.getMessage(), e);
        }
    }
}