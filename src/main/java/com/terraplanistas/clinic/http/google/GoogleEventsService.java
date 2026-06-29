package com.terraplanistas.clinic.http.google;

import com.google.api.services.calendar.model.*;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleEventsService extends GoogleApiService {

    public GoogleEventsService(GoogleTokenService tokenService) {
        super(tokenService);
    }

    public String listEvents(String googleUserId, String calendarId, String pageToken, Integer maxResults) {
        try {
            var calendar = createCalendarClient(googleUserId);
            var request = calendar.events().list(calendarId);

            if (pageToken != null) {
                request.setPageToken(pageToken);
            }
            if (maxResults != null) {
                request.setMaxResults(maxResults);
            }

            request.setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false);

            Events events = request.execute();
            return events.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to list events: " + e.getMessage(), e);
        }
    }

    public String getEvent(String googleUserId, String calendarId, String eventId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            Event event = calendar.events().get(calendarId, eventId).execute();
            return event.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to get event: " + e.getMessage(), e);
        }
    }

    public Event getEventObject(String googleUserId, String calendarId, String eventId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            return calendar.events().get(calendarId, eventId)
                    .setFields("id,summary,description,location,start,end,conferenceData,htmlLink")
                    .execute();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to get event: " + e.getMessage(), e);
        }
    }

    public String createEvent(
            String googleUserId,
            String calendarId,
            Event event
    ) {
        try {
            var calendar = createCalendarClient(googleUserId);

            Event result =
                    calendar.events()
                            .insert(calendarId, event)
                            .execute();

            return result.getId();

        } catch (IOException e) {
            throw new ExternalApiException(
                    "GOOGLE",
                    "Failed to create event: " + e.getMessage(),
                    e
            );
        }
    }

    public String updateEvent(String googleUserId, String calendarId, String eventId, Event event) {
        try {
            var calendar = createCalendarClient(googleUserId);
            Event result = calendar.events().update(calendarId, eventId, event).execute();
            return result.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to update event: " + e.getMessage(), e);
        }
    }

    public String patchEvent(String googleUserId, String calendarId, String eventId, Event event) {
        try {
            var calendar = createCalendarClient(googleUserId);
            Event result = calendar.events().patch(calendarId, eventId, event).execute();
            return result.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to patch event: " + e.getMessage(), e);
        }
    }

    public void deleteEvent(String googleUserId, String calendarId, String eventId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            calendar.events().delete(calendarId, eventId).execute();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to delete event: " + e.getMessage(), e);
        }
    }

    public String createMeetConference(String googleUserId, String calendarId, Event event) {
        try {
            var calendar = createCalendarClient(googleUserId);

            if (event.getConferenceData() == null) {
                ConferenceData conferenceData = new ConferenceData();
                CreateConferenceRequest createRequest = new CreateConferenceRequest();
                createRequest.setRequestId("meet-" + System.currentTimeMillis());
                conferenceData.setCreateRequest(createRequest);
                event.setConferenceData(conferenceData);
            }

            Event result = calendar.events()
                    .insert(calendarId, event)
                    .setConferenceDataVersion(1)
                    .setSendNotifications(true)
                    .execute();
            return result.getId();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to create meet conference: " + e.getMessage(), e);
        }
    }
}