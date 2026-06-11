package com.terraplanistas.clinic.http.credentials;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.terraplanistas.clinic.http.session.GoogleSession;
import com.terraplanistas.clinic.http.session.GoogleSessionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class GoogleTokenService {

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final GoogleClientSecretsLoader clientSecretsLoader;
    private final GoogleSessionService sessionService;

    public GoogleTokenService(NetHttpTransport httpTransport,
                              GoogleClientSecretsLoader clientSecretsLoader,
                              GoogleSessionService sessionService) {
        this.httpTransport = httpTransport;
        this.jsonFactory = com.terraplanistas.clinic.http.config.GoogleApiConfig.JSON_FACTORY;
        this.clientSecretsLoader = clientSecretsLoader;
        this.sessionService = sessionService;
    }

    public GoogleTokenStore exchangeCodeForTokens(String authorizationCode, String redirectUri) throws IOException {
        var clientSecrets = clientSecretsLoader.load("credentials.json");
        var clientId = clientSecrets.getDetails().getClientId();
        var clientSecret = clientSecrets.getDetails().getClientSecret();

        TokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                clientId,
                clientSecret,
                authorizationCode,
                redirectUri
        ).setScopes(List.of(
                "https://www.googleapis.com/auth/calendar",
                "https://www.googleapis.com/auth/calendar.events"
        )).execute();

        return new GoogleTokenStore(
                response.getAccessToken(),
                response.getRefreshToken(),
                System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000)
        );
    }

    public GoogleTokenStore refreshAccessToken(String googleUserId) throws IOException {
        Optional<GoogleSession> sessionOpt = sessionService.getSessionByGoogleUserId(googleUserId);

        if (sessionOpt.isEmpty() || !sessionOpt.get().hasRefreshToken()) {
            throw new IllegalStateException("No refresh token available for: " + googleUserId);
        }

        var refreshToken = sessionOpt.get().getRefreshToken();

        var clientSecrets = clientSecretsLoader.load("credentials.json");
        var clientId = clientSecrets.getDetails().getClientId();
        var clientSecret = clientSecrets.getDetails().getClientSecret();

        TokenResponse response = new GoogleRefreshTokenRequest(
                httpTransport,
                jsonFactory,
                clientId,
                clientSecret,
                refreshToken
        ).execute();

        sessionService.updateToken(
                googleUserId,
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getExpiresInSeconds()
        );

        return new GoogleTokenStore(
                response.getAccessToken(),
                response.getRefreshToken(),
                System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000)
        );
    }

    public GoogleTokenStore getStoredToken(String googleUserId) {
        Optional<GoogleSession> sessionOpt = sessionService.getSessionByGoogleUserId(googleUserId);
        if (sessionOpt.isEmpty()) {
            return new GoogleTokenStore(null, null, null);
        }
        GoogleSession session = sessionOpt.get();
        return new GoogleTokenStore(
                session.getAccessToken(),
                session.getRefreshToken(),
                session.getExpiresAt() != null
                        ? session.getExpiresAt().toInstant().toEpochMilli()
                        : null
        );
    }

    public void setStoredToken(String googleUserId, String accessToken, String refreshToken, Long expiresInSeconds) {
        sessionService.createSession(
                googleUserId,
                accessToken,
                refreshToken,
                expiresInSeconds,
                null
        );
    }
}