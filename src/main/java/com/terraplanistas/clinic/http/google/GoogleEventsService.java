package com.terraplanistas.clinic.http.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.terraplanistas.clinic.http.config.GoogleApiConfig;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.credentials.GoogleTokenStore;
import com.terraplanistas.clinic.http.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class GoogleEventsService {

    private static final JsonFactory JSON_FACTORY = GoogleApiConfig.JSON_FACTORY;

    private final GoogleTokenService tokenService;

    public GoogleEventsService(GoogleTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String listEvents(String googleUserId, String calendarId, String pageToken, Integer maxResults) throws Exception {
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
    }

    public String getEvent(String googleUserId, String calendarId, String eventId) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        Event event = calendar.events().get(calendarId, eventId).execute();
        return event.toString();
    }

    public String createEvent(String googleUserId, String calendarId, Event event) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        Event result = calendar.events().insert(calendarId, event).execute();
        return result.toString();
    }

    public String updateEvent(String googleUserId, String calendarId, String eventId, Event event) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        Event result = calendar.events().update(calendarId, eventId, event).execute();
        return result.toString();
    }

    public String patchEvent(String googleUserId, String calendarId, String eventId, Event event) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        Event result = calendar.events().patch(calendarId, eventId, event).execute();
        return result.toString();
    }

    public void deleteEvent(String googleUserId, String calendarId, String eventId) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        calendar.events().delete(calendarId, eventId).execute();
    }

    public String createMeetConference(String googleUserId, String calendarId, Event event) throws Exception {
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
        return result.toString();
    }

    private Calendar createCalendarClient(String googleUserId) throws Exception {
        var tokenStore = getValidTokenStore(googleUserId);
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(tokenStore.getAccessToken())
                .setRefreshToken(tokenStore.getRefreshToken())
                .setExpirationTimeMilliseconds(tokenStore.getExpiresAt());

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("Clinic API")
                .build();
    }

    private GoogleTokenStore getValidTokenStore(String googleUserId) throws Exception {
        var token = tokenService.getStoredToken(googleUserId);
        if (token == null || !token.hasRefreshToken()) {
            throw new IllegalStateException("No token available. Please authenticate first.");
        }
        if (token.isExpired()) {
            token = tokenService.refreshAccessToken(googleUserId);
        }
        return token;
    }

    private TokenResponse getValidToken(String googleUserId) throws Exception {
        var token = tokenService.getStoredToken(googleUserId);
        if (token == null || !token.hasRefreshToken()) {
            throw new IllegalStateException("No token available. Please authenticate first.");
        }
        if (token.isExpired()) {
            token = tokenService.refreshAccessToken(googleUserId);
        }
        return TokenResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn((token.getExpiresAt() - System.currentTimeMillis()) / 1000)
                .tokenType(token.getTokenType())
                .build();
    }
}