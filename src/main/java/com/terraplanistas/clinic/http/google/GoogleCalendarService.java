package com.terraplanistas.clinic.http.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.terraplanistas.clinic.http.config.GoogleApiConfig;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.credentials.GoogleTokenStore;
import com.terraplanistas.clinic.http.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Clinic API";
    private static final JsonFactory JSON_FACTORY = GoogleApiConfig.JSON_FACTORY;

    private final GoogleTokenService tokenService;

    public GoogleCalendarService(GoogleTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public Calendar createCalendarClient(String googleUserId) throws Exception {
        var tokenStore = getValidTokenStore(googleUserId);
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(tokenStore.getAccessToken())
                .setRefreshToken(tokenStore.getRefreshToken())
                .setExpirationTimeMilliseconds(tokenStore.getExpiresAt());

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public TokenResponse getValidToken(String googleUserId) throws Exception {
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

    public String listCalendars(String googleUserId) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        var calendars = calendar.calendarList().list().execute();
        return calendars.toString();
    }

    public String getCalendar(String googleUserId, String calendarId) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        var cal = calendar.calendars().get(calendarId).execute();
        return cal.toString();
    }

    public String createCalendar(String googleUserId, String summary, String description) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        var calendarEntry = new com.google.api.services.calendar.model.Calendar()
                .setSummary(summary)
                .setDescription(description);
        var result = calendar.calendars().insert(calendarEntry).execute();
        return result.toString();
    }

    public String updateCalendar(String googleUserId, String calendarId, String summary, String description) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        var calendarEntry = new com.google.api.services.calendar.model.Calendar()
                .setSummary(summary)
                .setDescription(description);
        var result = calendar.calendars().patch(calendarId, calendarEntry).execute();
        return result.toString();
    }

    public void deleteCalendar(String googleUserId, String calendarId) throws Exception {
        var calendar = createCalendarClient(googleUserId);
        calendar.calendars().delete(calendarId).execute();
    }
}