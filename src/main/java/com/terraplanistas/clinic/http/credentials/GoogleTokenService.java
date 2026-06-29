package com.terraplanistas.clinic.http.credentials;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.terraplanistas.clinic.domain.entities.OAuthToken;
import com.terraplanistas.clinic.repositories.OAuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class GoogleTokenService {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final GoogleClientSecretsLoader clientSecretsLoader;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OAuthTokenRepository oauthTokenRepository;

    public GoogleTokenService(NetHttpTransport httpTransport,
                              GoogleClientSecretsLoader clientSecretsLoader,
                              OAuth2AuthorizedClientService authorizedClientService,
                              OAuth2AuthorizedClientRepository authorizedClientRepository,
                              OAuthTokenRepository oauthTokenRepository) {
        this.httpTransport = httpTransport;
        this.jsonFactory = com.terraplanistas.clinic.http.config.GoogleApiConfig.JSON_FACTORY;
        this.clientSecretsLoader = clientSecretsLoader;
        this.authorizedClientService = authorizedClientService;
        this.authorizedClientRepository = authorizedClientRepository;
        this.oauthTokenRepository = oauthTokenRepository;
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
        String refreshToken = null;

        Optional<OAuthToken> dbToken = oauthTokenRepository.findByGoogleUserId(googleUserId);
        if (dbToken.isPresent()) {
            refreshToken = dbToken.get().getRefreshToken();
        }

        if (refreshToken == null) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    GOOGLE_REGISTRATION_ID, googleUserId);
            if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
                refreshToken = authorizedClient.getRefreshToken().getTokenValue();
            }
        }

        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available for: " + googleUserId);
        }

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

        GoogleTokenStore newToken = new GoogleTokenStore(
                response.getAccessToken(),
                response.getRefreshToken(),
                System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000)
        );

        saveAuthorizedClient(googleUserId, newToken.getAccessToken(),
                newToken.getRefreshToken(), newToken.getExpiresAt());

        return newToken;
    }

    public GoogleTokenStore getStoredToken(String googleUserId) {
        Optional<OAuthToken> dbToken = oauthTokenRepository.findByGoogleUserId(googleUserId);
        if (dbToken.isPresent()) {
            OAuthToken token = dbToken.get();
            return new GoogleTokenStore(
                    token.getAccessToken(),
                    token.getRefreshToken(),
                    token.getExpiresAt()
            );
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                GOOGLE_REGISTRATION_ID, googleUserId);

        if (authorizedClient == null) {
            return new GoogleTokenStore(null, null, null);
        }

        return new GoogleTokenStore(
                authorizedClient.getAccessToken().getTokenValue(),
                authorizedClient.getRefreshToken() != null
                        ? authorizedClient.getRefreshToken().getTokenValue()
                        : null,
                authorizedClient.getAccessToken().getExpiresAt() != null
                        ? authorizedClient.getAccessToken().getExpiresAt().toEpochMilli()
                        : null
        );
    }

    public void saveAuthorizedClient(String googleUserId, String accessToken, String refreshToken, Long expiresInSeconds) {
        OAuthToken token = oauthTokenRepository.findByGoogleUserId(googleUserId)
                .orElse(new OAuthToken());
        token.setGoogleUserId(googleUserId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(expiresInSeconds);
        oauthTokenRepository.save(token);
    }

    public void saveAuthorizedClient(String googleUserId, Authentication authentication, HttpServletRequest request) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
                GOOGLE_REGISTRATION_ID, authentication, request);
        if (authorizedClient != null) {
            saveAuthorizedClient(
                    googleUserId,
                    authorizedClient.getAccessToken().getTokenValue(),
                    authorizedClient.getRefreshToken() != null
                            ? authorizedClient.getRefreshToken().getTokenValue()
                            : null,
                    authorizedClient.getAccessToken().getExpiresAt() != null
                            ? authorizedClient.getAccessToken().getExpiresAt().toEpochMilli()
                            : null
            );
            return;
        }
        throw new IllegalStateException("No authorized client found in session for googleUserId: " + googleUserId);
    }

    public void exchangeAndSaveTokens(String googleUserId, String authorizationCode, String redirectUri) {
        try {
            GoogleTokenStore tokenStore = exchangeCodeForTokens(authorizationCode, redirectUri);
            saveAuthorizedClient(
                    googleUserId,
                    tokenStore.getAccessToken(),
                    tokenStore.getRefreshToken(),
                    tokenStore.getExpiresAt()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to exchange authorization code for tokens: " + e.getMessage(), e);
        }
    }
}
