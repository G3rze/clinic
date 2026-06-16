package com.terraplanistas.clinic.http.google;

import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleMeetService extends GoogleApiService {

    public GoogleMeetService(GoogleTokenService tokenService) {
        super(tokenService);
    }

    public String createConference(String googleUserId, String calendarId, String eventId) {
        try {
            var calendar = createCalendarClient(googleUserId);

            Event event = calendar.events().get(calendarId, eventId).execute();

            ConferenceData conferenceData = new ConferenceData();
            CreateConferenceRequest createRequest = new CreateConferenceRequest();
            createRequest.setRequestId("meet-" + System.currentTimeMillis());
            conferenceData.setCreateRequest(createRequest);

            event.setConferenceData(conferenceData);

            Event result = calendar.events()
                    .patch(calendarId, eventId, event)
                    .setConferenceDataVersion(1)
                    .setSendNotifications(true)
                    .execute();

            return result.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to create conference: " + e.getMessage(), e);
        }
    }

    public String getConferenceData(String googleUserId, String calendarId, String eventId) {
        try {
            var calendar = createCalendarClient(googleUserId);
            Event event = calendar.events().get(calendarId, eventId)
                    .setFields("conferenceData")
                    .execute();
            return event.toString();
        } catch (IOException e) {
            throw new ExternalApiException("GOOGLE", "Failed to get conference data: " + e.getMessage(), e);
        }
    }

    public String joinMeeting(String meetingId) {
        return "https://meet.google.com/" + meetingId;
    }

    public String extractMeetLink(String conferenceDataString) {
        return null;
    }
}