package com.terraplanistas.clinic.http.controller;

import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.http.dto.GoogleTokenRequest;
import com.terraplanistas.clinic.http.dto.TokenResponse;
import com.terraplanistas.clinic.http.google.GoogleAuthService;
import com.terraplanistas.clinic.http.session.GoogleSession;
import com.terraplanistas.clinic.http.session.GoogleSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
public class AuthController {

    private final GoogleAuthService authService;
    private final GoogleSessionService sessionService;

    public AuthController(GoogleAuthService authService, GoogleSessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @GetMapping("/url")
    public ResponseEntity<ApiResponse<String>> getAuthorizationUrl() {
        try {
            String url = authService.buildAuthorizationUrl();
            return ResponseEntity.ok(ApiResponse.success(url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to build authorization URL: " + e.getMessage(), "/auth/google/url"));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<GoogleSessionResponse>> callback(@RequestBody GoogleTokenRequest request) {
        try {
            TokenResponse tokenResponse = authService.exchangeCodeForTokens(request.getCode(), request.getRedirectUri());

            String googleUserId = extractGoogleUserId(tokenResponse.getAccessToken());

            GoogleSession session = sessionService.createSession(
                    googleUserId,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn(),
                    tokenResponse.getScope()
            );

            GoogleSessionResponse response = new GoogleSessionResponse(
                    session.getId().toString(),
                    googleUserId,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Authentication failed: " + e.getMessage(), "/auth/google/callback"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(value = "X-Google-User-Id", required = false) String googleUserId,
                                                       @RequestParam(value = "userId", required = false) UUID userId) {
        try {
            if (googleUserId != null) {
                sessionService.deleteSessionByGoogleUserId(googleUserId);
            } else if (userId != null) {
                sessionService.deleteSessionByUserId(userId);
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "Either X-Google-User-Id header or userId parameter is required", "/auth/google/logout"));
            }
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Logout failed: " + e.getMessage(), "/auth/google/logout"));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> getSessionStatus(@RequestParam(required = false) UUID userId,
                                                                               @RequestParam(required = false) String googleUserId) {
        try {
            GoogleSession session = null;

            if (userId != null) {
                session = sessionService.getSessionByUserId(userId).orElse(null);
            } else if (googleUserId != null) {
                session = sessionService.getSessionByGoogleUserId(googleUserId).orElse(null);
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "Either userId or googleUserId parameter is required", "/auth/google/session"));
            }

            if (session == null) {
                return ResponseEntity.ok(ApiResponse.success(new SessionStatusResponse(false, null, null, null)));
            }

            boolean isExpired = session.isExpired();
            return ResponseEntity.ok(ApiResponse.success(new SessionStatusResponse(
                    !isExpired,
                    session.getGoogleUserId(),
                    session.getGoogleEmail(),
                    session.getExpiresAt()
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to get session status: " + e.getMessage(), "/auth/google/session"));
        }
    }

    @GetMapping("/mock/login")
    public ResponseEntity<ApiResponse<String>> mockLoginPage() {
        String mockHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Google Login - Mock</title>
                    <style>
                        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f5f5f5; }
                        .login-container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                        h1 { color: #333; margin-bottom: 20px; }
                        p { color: #666; margin-bottom: 30px; }
                        .btn { background-color: #4285f4; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; text-decoration: none; display: inline-block; }
                        .btn:hover { background-color: #357ae8; }
                    </style>
                </head>
                <body>
                    <div class="login-container">
                        <h1>Clinic App - Google Login</h1>
                        <p>This is a mock login page. In production, this would redirect to Google OAuth.</p>
                        <a href="/auth/google/url" class="btn">Login with Google</a>
                    </div>
                </body>
                </html>
                """;
        return ResponseEntity.ok(ApiResponse.success(mockHtml));
    }

    private String extractGoogleUserId(String accessToken) {
        return "user-" + accessToken.hashCode();
    }

    public record GoogleSessionResponse(
            String sessionId,
            String googleUserId,
            String accessToken,
            String refreshToken,
            Long expiresIn
    ) {}

    public record SessionStatusResponse(
            boolean active,
            String googleUserId,
            String email,
            java.time.OffsetDateTime expiresAt
    ) {}
}