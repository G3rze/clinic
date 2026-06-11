package com.terraplanistas.clinic.http.credentials;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class GoogleClientSecretsLoader {

    public GoogleClientSecrets load(String credentialsPath) throws IOException {
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