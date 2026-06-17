package com.terraplanistas.clinic.http.security.service;

import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.JwtProperties;
import com.terraplanistas.clinic.http.session.GoogleSession;
import com.terraplanistas.clinic.http.session.GoogleSessionService;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final JwtTokenService jwtTokenService;
    private final GoogleSessionService sessionService;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(JwtTokenService jwtTokenService,
                               GoogleSessionService sessionService,
                               JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.sessionService = sessionService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public Map<String, Object> refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        try {
            String tokenType = jwtTokenService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new IllegalArgumentException("Invalid token type: expected refresh token");
            }

            if (jwtTokenService.isTokenExpired(refreshToken)) {
                throw new IllegalArgumentException("Refresh token has expired");
            }

            UUID userId = jwtTokenService.getUserIdFromToken(refreshToken);

            Optional<GoogleSession> sessionOpt = sessionService.findActiveJwtSessionByUserId(userId);
            if (sessionOpt.isEmpty()) {
                throw new IllegalArgumentException("No active session found");
            }

            GoogleSession session = sessionOpt.get();

            if (!refreshToken.equals(session.getJwtRefreshToken())) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            String email = session.getGoogleEmail();
            java.util.List<String> roles = jwtTokenService.getRoles(refreshToken);

            String newAccessToken = jwtTokenService.generateAccessToken(userId, email, roles);
            String newRefreshToken = jwtTokenService.generateRefreshToken(userId);

            OffsetDateTime jwtExpiresAt = OffsetDateTime.now().plusNanos(
                    jwtProperties.getAccessTokenExpirationMs() * 1_000_000
            );

            sessionService.updateJwtTokens(userId, newAccessToken, newRefreshToken, jwtExpiresAt);

            Map<String, Object> result = new HashMap<>();
            result.put("access_token", newAccessToken);
            result.put("refresh_token", newRefreshToken);
            result.put("token_type", "Bearer");
            result.put("expires_in", jwtProperties.getAccessTokenExpirationMs() / 1000);

            return result;

        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
        }
    }

    @Transactional
    public void invalidateSession(UUID userId) {
        sessionService.clearJwtTokensByUserId(userId);
    }
}