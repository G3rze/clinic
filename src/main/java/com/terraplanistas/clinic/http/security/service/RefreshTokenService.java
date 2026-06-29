package com.terraplanistas.clinic.http.security.service;

import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.JwtProperties;
import com.terraplanistas.clinic.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(JwtTokenService jwtTokenService,
                               UserRepository userRepository,
                               JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

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

            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("User not found");
            }

            var user = userOpt.get();
            var roles = jwtTokenService.getRoles(refreshToken);

            String newAccessToken = jwtTokenService.generateAccessToken(userId, user.getEmail(), roles);
            String newRefreshToken = jwtTokenService.generateRefreshToken(userId);

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

    public void invalidateSession(UUID userId) {
    }
}
