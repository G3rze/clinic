package com.terraplanistas.clinic.http.security;

import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class CustomOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2AccessTokenResponseClient.class);

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate =
            new RestClientAuthorizationCodeTokenResponseClient();
    private final GoogleTokenService googleTokenService;
    private final RestTemplate restTemplate;

    public CustomOAuth2AccessTokenResponseClient(GoogleTokenService googleTokenService,
                                                 RestTemplate restTemplate) {
        this.googleTokenService = googleTokenService;
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationGrantRequest);

        String accessToken = response.getAccessToken().getTokenValue();
        String refreshToken = response.getRefreshToken() != null
                ? response.getRefreshToken().getTokenValue()
                : null;
        Long expiresAt = response.getAccessToken().getExpiresAt() != null
                ? response.getAccessToken().getExpiresAt().toEpochMilli()
                : null;

        String googleUserId = getGoogleUserIdFromUserInfoEndpoint(accessToken);

        if (googleUserId != null) {
            try {
                googleTokenService.saveAuthorizedClient(googleUserId, accessToken, refreshToken, expiresAt);
                log.info("Saved Google tokens for googleUserId={}", googleUserId);
            } catch (Exception e) {
                log.error("Failed to save Google tokens for googleUserId={}: {}", googleUserId, e.getMessage());
            }
        } else {
            log.warn("Could not extract googleUserId from userinfo endpoint");
        }

        return response;
    }

    private String getGoogleUserIdFromUserInfoEndpoint(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (responseEntity.getBody() != null) {
                return (String) responseEntity.getBody().get("sub");
            }
        } catch (Exception e) {
            log.error("Failed to get googleUserId from userinfo endpoint: {}", e.getMessage());
        }
        return null;
    }
}
