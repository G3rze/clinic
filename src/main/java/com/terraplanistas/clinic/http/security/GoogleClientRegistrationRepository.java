package com.terraplanistas.clinic.http.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.terraplanistas.clinic.http.credentials.GoogleClientSecretsLoader;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GoogleClientRegistrationRepository implements org.springframework.security.oauth2.client.registration.ClientRegistrationRepository {

    private static final String REGISTRATION_ID = "google";

    private final ClientRegistration clientRegistration;

    public GoogleClientRegistrationRepository(GoogleClientSecretsLoader secretsLoader) {
        try {
            GoogleClientSecrets secrets = secretsLoader.load("credentials.json");
            GoogleClientSecrets.Details web = secrets.getWeb();
            if (web == null) {
                throw new IllegalStateException(
                        "Invalid credentials.json: 'web' configuration not found");
            }

            this.clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
                    .clientId(web.getClientId())
                    .clientSecret(web.getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope(List.of(
                            "openid",
                            "profile",
                            "email",
                            "https://www.googleapis.com/auth/calendar",
                            "https://www.googleapis.com/auth/calendar.events"
                    ))
                    .authorizationUri(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL)
                    .tokenUri(GoogleOAuthConstants.TOKEN_SERVER_URL)
                    .userNameAttributeName(IdTokenClaimNames.SUB)
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load Google credentials from credentials.json", e);
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (REGISTRATION_ID.equals(registrationId)) {
            return clientRegistration;
        }
        return null;
    }
}