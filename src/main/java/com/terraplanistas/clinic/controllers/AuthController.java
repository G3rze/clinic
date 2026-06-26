package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.InitRegistrationRequest;
import com.terraplanistas.clinic.domain.entities.PendingUserConfig;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.service.RefreshTokenService;
import com.terraplanistas.clinic.repositories.PendingUserConfigRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.base-uri}/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PendingUserConfigRepository pendingUserConfigRepository;
    private final RegistrationService registrationService;

    public AuthController(RefreshTokenService refreshTokenService, JwtTokenService jwtTokenService,
                         UserRepository userRepository, PendingUserConfigRepository pendingUserConfigRepository,
                         RegistrationService registrationService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.pendingUserConfigRepository = pendingUserConfigRepository;
        this.registrationService = registrationService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return handleJwtAuthentication(authHeader);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            return handleOAuthAuthentication(oauthToken);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    private ResponseEntity<?> handleJwtAuthentication(String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtTokenService.getUserIdFromToken(token);
            String pendingUserConfigId = jwtTokenService.getPendingUserConfigId(token);

            var userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return buildUserResponse(userOpt.get(), pendingUserConfigId);
            }
            return ResponseEntity.status(404).body(Map.<String, Object>of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.<String, Object>of("error", "Invalid token"));
        }
    }

    private ResponseEntity<?> handleOAuthAuthentication(OAuth2AuthenticationToken oauthToken) {
        String googleUserId = oauthToken.getPrincipal().getAttribute("sub");
        String email = oauthToken.getPrincipal().getAttribute("email");

        if (googleUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No Google user ID"));
        }

        return userRepository.findByGoogleUserId(googleUserId)
                .map(user -> {
                    List<String> roles = user.getRole() != null ? List.of(user.getRole().getCode()) : List.of("USER");

                    Optional<PendingUserConfig> config = pendingUserConfigRepository.findByGoogleUserId(googleUserId);

                    String accountStatus;
                    String requiresAction;
                    String pendingUserConfigId = null;

                    if (config.isPresent()) {
                        pendingUserConfigId = config.get().getId().toString();
                        if (!config.get().isConsentGiven()) {
                            accountStatus = "consent_required";
                            requiresAction = "give_consent";
                        } else if (!config.get().isProfileComplete()) {
                            accountStatus = "profile_incomplete";
                            requiresAction = "complete_patient_profile";
                        } else {
                            accountStatus = "active";
                            requiresAction = "";
                        }
                    } else {
                        accountStatus = "active";
                        requiresAction = "";
                    }

                    String jwtAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), roles, pendingUserConfigId);
                    String jwtRefreshToken = jwtTokenService.generateRefreshToken(user.getId());

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("access_token", jwtAccessToken);
                    response.put("refresh_token", jwtRefreshToken);
                    response.put("token_type", "Bearer");
                    response.put("expires_in", jwtTokenService.getAccessTokenExpirationMs() / 1000);

                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId().toString());
                    userMap.put("google_user_id", user.getGoogleUserId() != null ? user.getGoogleUserId() : "");
                    userMap.put("name", user.getUsername() != null ? user.getUsername() : "");
                    userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
                    userMap.put("roles", roles);
                    userMap.put("account_status", accountStatus);
                    userMap.put("requires_action", requiresAction);
                    if (pendingUserConfigId != null) {
                        userMap.put("pendingUserConfigId", pendingUserConfigId);
                    }

                    response.put("user", userMap);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }

    private ResponseEntity<?> buildUserResponse(User user, String pendingUserConfigId) {
        List<String> roles = user.getRole() != null ? List.of(user.getRole().getCode()) : List.of("USER");

        String accountStatus;
        String requiresAction;

        if (pendingUserConfigId != null && !pendingUserConfigId.isBlank()) {
            Optional<PendingUserConfig> config = pendingUserConfigRepository.findById(UUID.fromString(pendingUserConfigId));
            if (config.isPresent()) {
                if (!config.get().isConsentGiven()) {
                    accountStatus = "consent_required";
                    requiresAction = "give_consent";
                } else if (!config.get().isProfileComplete()) {
                    accountStatus = "profile_incomplete";
                    requiresAction = "complete_patient_profile";
                } else {
                    accountStatus = "active";
                    requiresAction = "";
                }
            } else {
                accountStatus = "active";
                requiresAction = "";
            }
        } else {
            accountStatus = "active";
            requiresAction = "";
        }

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId().toString());
        userMap.put("google_user_id", user.getGoogleUserId() != null ? user.getGoogleUserId() : "");
        userMap.put("name", user.getUsername() != null ? user.getUsername() : "");
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("roles", roles);
        userMap.put("account_status", accountStatus);
        userMap.put("requires_action", requiresAction);
        if (pendingUserConfigId != null && !pendingUserConfigId.isBlank()) {
            userMap.put("pendingUserConfigId", pendingUserConfigId);
        }

        return ResponseEntity.ok(userMap);
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

    @PostMapping("/init-registration")
    public ResponseEntity<?> initRegistration(@Valid @RequestBody InitRegistrationRequest request) {
        try {
            var response = registrationService.initRegistration(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit-consent")
    public ResponseEntity<?> submitConsent(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            String pendingUserConfigId = jwtTokenService.getPendingUserConfigId(token);

            if (pendingUserConfigId == null || pendingUserConfigId.isBlank()) {
                return ResponseEntity.status(400).body(Map.of("error", "No pending registration found in token"));
            }

            var response = registrationService.submitConsent(pendingUserConfigId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
