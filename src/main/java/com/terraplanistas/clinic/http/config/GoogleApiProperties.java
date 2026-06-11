package com.terraplanistas.clinic.http.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google")
@Getter
@Setter
public class GoogleApiProperties {

    private String baseUrl = "https://www.googleapis.com";
    private Credentials credentials = new Credentials();

    @Getter
    @Setter
    public static class Credentials {
        private String path = "credentials.json";
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
}