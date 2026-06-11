package com.terraplanistas.clinic.http.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.terraplanistas.clinic.http.dto.TokenResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final GoogleAuthorizationCodeFlow authorizationCodeFlow;
    private final GoogleClientSecrets clientSecrets;
    private final JsonFactory jsonFactory;
    private final NetHttpTransport httpTransport;

    public GoogleAuthService(GoogleAuthorizationCodeFlow authorizationCodeFlow,
                             GoogleClientSecrets clientSecrets) {
        this.authorizationCodeFlow = authorizationCodeFlow;
        this.clientSecrets = clientSecrets;
        this.jsonFactory = com.terraplanistas.clinic.http.config.GoogleApiConfig.JSON_FACTORY;
        this.httpTransport = new NetHttpTransport();
    }

    public String buildAuthorizationUrl() throws IOException {
        return authorizationCodeFlow.newAuthorizationUrl()
                .setRedirectUri(getRedirectUri())
                .setState(UUID.randomUUID().toString())
                .build();
    }

    public TokenResponse exchangeCodeForTokens(String authorizationCode, String redirectUri) throws IOException {
        var tokenResponse = authorizationCodeFlow
                .newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri)
                .execute();

        return TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresInSeconds())
                .tokenType(tokenResponse.getTokenType())
                .scope(tokenResponse.getScope())
                .build();
    }

    public HttpRequestFactory createRequestFactory(String accessToken) {
        return httpTransport.createRequestFactory(request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        });
    }

    private String getRedirectUri() {
        var redirectUris = clientSecrets.getDetails().getRedirectUris();
        if (redirectUris != null && !redirectUris.isEmpty()) {
            return redirectUris.get(0);
        }
        return "http://localhost:8080/oauth/callback";
    }
}