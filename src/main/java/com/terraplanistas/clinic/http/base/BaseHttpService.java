package com.terraplanistas.clinic.http.base;

import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;

public abstract class BaseHttpService implements ApiService {

    protected final RestClient restClient;
    protected final String baseUrl;
    protected final String authToken;

    protected BaseHttpService(RestClient restClient, String baseUrl, String authToken) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    @Override
    public String getProvider() {
        return getProviderName();
    }

    protected abstract String getProviderName();

    protected URI buildUri(String path) {
        return URI.create(baseUrl + path);
    }

    protected <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        return restClient.get()
                .uri(buildUri(path))
                .headers(headers -> headers.setAll(buildHeaders()))
                .retrieve()
                .onStatus(statusCode -> statusCode.is4xxClientError(), (request, response) -> {
                    throw new ExternalApiException(getProviderName(), "Client error: " + response.getStatusCode(), response.getStatusCode().value());
                })
                .onStatus(statusCode -> statusCode.is5xxServerError(), (request, response) -> {
                    throw new ExternalApiException(getProviderName(), "Server error: " + response.getStatusCode(), response.getStatusCode().value());
                })
                .toEntity(responseType);
    }

    protected <T, R> ResponseEntity<T> post(String path, R body, Class<T> responseType) {
        return restClient.post()
                .uri(buildUri(path))
                .headers(headers -> headers.setAll(buildHeaders()))
                .body(body)
                .retrieve()
                .onStatus(statusCode -> statusCode.is4xxClientError(), (request, response) -> {
                    throw new ExternalApiException(getProviderName(), "Client error: " + response.getStatusCode(), response.getStatusCode().value());
                })
                .onStatus(statusCode -> statusCode.is5xxServerError(), (request, response) -> {
                    throw new ExternalApiException(getProviderName(), "Server error: " + response.getStatusCode(), response.getStatusCode().value());
                })
                .toEntity(responseType);
    }

    protected java.util.Map<String, String> buildHeaders() {
        return java.util.Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer " + authToken
        );
    }
}
