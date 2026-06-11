package com.terraplanistas.clinic.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String scope;

    public static TokenResponse fromCredentials(
            String accessToken,
            String refreshToken,
            Long expiresInSeconds,
            String tokenType,
            String scope) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresInSeconds)
                .tokenType(tokenType)
                .scope(scope)
                .build();
    }
}