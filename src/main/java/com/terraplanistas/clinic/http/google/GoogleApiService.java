package com.terraplanistas.clinic.http.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.terraplanistas.clinic.http.config.GoogleApiConfig;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.http.credentials.GoogleTokenStore;
import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import org.springframework.stereotype.Service;

@Service
public abstract class GoogleApiService {

    private static final String APPLICATION_NAME = "Clinic API";
    private static final JsonFactory JSON_FACTORY = GoogleApiConfig.JSON_FACTORY;

    protected final GoogleTokenService tokenService;

    protected GoogleApiService(GoogleTokenService tokenService) {
        this.tokenService = tokenService;
    }

    protected Calendar createCalendarClient(String googleUserId) {
        try {
            var tokenStore = getValidTokenStore(googleUserId);
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(tokenStore.getAccessToken())
                    .setRefreshToken(tokenStore.getRefreshToken())
                    .setExpirationTimeMilliseconds(tokenStore.getExpiresAt());

            return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            throw new ExternalApiException("GOOGLE", "Failed to create calendar client: " + e.getMessage(), e);
        }
    }

    protected GoogleTokenStore getValidTokenStore(String googleUserId) {
        try {
            var token = tokenService.getStoredToken(googleUserId);
            if (token == null || token.getAccessToken() == null || token.getAccessToken().isEmpty()) {
                throw new IllegalStateException("No token available. Please authenticate first.");
            }
            return token;
        } catch (IllegalStateException e) {
            throw new ExternalApiException("GOOGLE", e.getMessage(), 401, "UNAUTHORIZED");
        } catch (Exception e) {
            throw new ExternalApiException("GOOGLE", "Failed to get valid token: " + e.getMessage(), e);
        }
    }

    protected GoogleTokenStore getValidToken(String googleUserId) {
        return getValidTokenStore(googleUserId);
    }
}