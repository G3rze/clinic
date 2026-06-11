package com.terraplanistas.clinic.http.credentials;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenStore {

    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
    private String tokenType;

    public GoogleTokenStore() {}

    public GoogleTokenStore(String accessToken, String refreshToken, Long expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.tokenType = "Bearer";
    }

    public boolean isExpired() {
        return expiresAt != null && System.currentTimeMillis() >= expiresAt;
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }
}