package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;

    public AuthController(RefreshTokenService refreshTokenService, JwtTokenService jwtTokenService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "refresh_token is required"));
        }

        try {
            Map<String, Object> tokens = refreshTokenService.refreshTokens(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String accessToken = request.get("access_token");

        if (accessToken == null || accessToken.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                UUID userId = (UUID) auth.getPrincipal();
                refreshTokenService.invalidateSession(userId);
            }
        } else {
            try {
                UUID userId = jwtTokenService.getUserIdFromToken(accessToken);
                refreshTokenService.invalidateSession(userId);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid access token"));
            }
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}