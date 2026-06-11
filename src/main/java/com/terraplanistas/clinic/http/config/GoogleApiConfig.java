package com.terraplanistas.clinic.http.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.terraplanistas.clinic.http.credentials.GoogleClientSecretsLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleApiConfig {

    public static final String APPLICATION_NAME = "Clinic API";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/calendar.events"
    );

    @Bean
    public NetHttpTransport httpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public GoogleClientSecrets googleClientSecrets(GoogleClientSecretsLoader secretsLoader) throws IOException {
        return secretsLoader.load("credentials.json");
    }

    @Bean
    public GoogleAuthorizationCodeFlow authorizationCodeFlow(NetHttpTransport httpTransport,
                                                             GoogleClientSecrets clientSecrets) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }
}