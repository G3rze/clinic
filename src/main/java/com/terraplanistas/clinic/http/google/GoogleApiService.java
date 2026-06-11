package com.terraplanistas.clinic.http.google;

import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import org.springframework.stereotype.Service;

@Service
public abstract class GoogleApiService {

    protected final GoogleTokenService tokenService;

    protected GoogleApiService(GoogleTokenService tokenService) {
        this.tokenService = tokenService;
    }

    protected abstract AbstractGoogleJsonClient getClient() throws Exception;

    protected abstract String getBaseUrl();
}