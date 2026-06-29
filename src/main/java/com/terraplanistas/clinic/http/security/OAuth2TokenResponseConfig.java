package com.terraplanistas.clinic.http.security;

import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2TokenResponseConfig {

    private final GoogleTokenService googleTokenService;
    private final RestTemplate restTemplate;

    public OAuth2TokenResponseConfig(GoogleTokenService googleTokenService, RestTemplate restTemplate) {
        this.googleTokenService = googleTokenService;
        this.restTemplate = restTemplate;
    }

    @Bean
    @Primary
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new CustomOAuth2AccessTokenResponseClient(googleTokenService, restTemplate);
    }
}