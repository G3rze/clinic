package com.terraplanistas.clinic.http.credentials;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleClientSecretsLoader {

    private static final String CREDENTIALS_ENV_VAR = "GOOGLE_CREDENTIALS_JSON";

    public GoogleClientSecrets load(String credentialsPath) throws IOException {
        String jsonContent = System.getenv(CREDENTIALS_ENV_VAR);

        if (jsonContent != null && !jsonContent.isBlank()) {
            try (var inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
                 var reader = new InputStreamReader(inputStream)) {
                return GoogleClientSecrets.load(
                        com.terraplanistas.clinic.http.config.GoogleApiConfig.JSON_FACTORY,
                        reader
                );
            }
        }

        var resource = new ClassPathResource(credentialsPath);
        try (var inputStream = resource.getInputStream();
             var reader = new InputStreamReader(inputStream)) {
            return GoogleClientSecrets.load(
                    com.terraplanistas.clinic.http.config.GoogleApiConfig.JSON_FACTORY,
                    reader
            );
        }
    }
}