package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.InitRegistrationRequest;
import com.terraplanistas.clinic.domain.entities.PendingUserConfig;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.http.config.AppStripeProperties;
import com.terraplanistas.clinic.http.security.CookieService;
import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.service.RefreshTokenService;
import com.terraplanistas.clinic.repositories.PendingUserConfigRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/auth")
@Tag(name = "Autenticación y Registro",
        description = "Endpoints para la gestión de autenticación de usuarios mediante JWT y OAuth2 (Google). " +
                "Incluye obtención de perfil del usuario actual, renovación de tokens, cierre de sesión, " +
                "inicio de registro de nuevos pacientes y gestión de consentimientos informados.")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PendingUserConfigRepository pendingUserConfigRepository;
    private final RegistrationService registrationService;
    private final CookieService cookieService;
    private final AppStripeProperties appStripeProperties;

    public AuthController(RefreshTokenService refreshTokenService, JwtTokenService jwtTokenService,
                          UserRepository userRepository, PendingUserConfigRepository pendingUserConfigRepository,
                          RegistrationService registrationService, CookieService cookieService,
                          AppStripeProperties appStripeProperties) {
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.pendingUserConfigRepository = pendingUserConfigRepository;
        this.registrationService = registrationService;
        this.cookieService = cookieService;
        this.appStripeProperties = appStripeProperties;
    }

    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            description = "Recupera la información del perfil del usuario actualmente autenticado en el sistema. " +
                    "Soporta múltiples métodos de autenticación:\n" +
                    "- **JWT**: Mediante header Authorization Bearer token\n" +
                    "- **Cookie**: Token de acceso almacenado en cookies HTTP-only\n" +
                    "- **OAuth2/Google**: Usuario autenticado mediante Google\n\n" +
                    "La respuesta incluye el estado de la cuenta y las acciones requeridas por el usuario, como:\n" +
                    "- `consent_required`: Debe aceptar los consentimientos informados\n" +
                    "- `profile_incomplete`: Debe completar su perfil de paciente\n" +
                    "- `active`: Cuenta completamente operativa"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil de usuario recuperado exitosamente. Incluye datos del usuario, roles, " +
                            "estado de cuenta y funcionalidades habilitadas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. No se encontró un token válido en el header ni en cookies",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado en el sistema para el identificador proporcionado",
                    content = @Content
            )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @Parameter(description = "Header de autorización con el token JWT en formato 'Bearer {token}'. " +
                    "Opcional si se utilizan cookies de autenticación")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) HttpServletRequest request) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return handleJwtAuthentication(authHeader);
        }

        String cookieToken = cookieService.getAccessTokenFromRequest(request);
        if (cookieToken != null) {
            return handleJwtAuthentication("Bearer " + cookieToken);
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
                    userMap.put("features", Map.of("stripeEnabled", appStripeProperties.isEnabled()));

                    return ResponseEntity.ok(userMap);
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
        userMap.put("features", Map.of("stripeEnabled", appStripeProperties.isEnabled()));

        return ResponseEntity.ok(userMap);
    }

    @Operation(
            summary = "Renovar tokens de acceso",
            description = "Renueva el token JWT de acceso y el refresh token utilizando el refresh token " +
                    "almacenado en las cookies HTTP-only. Los nuevos tokens se establecen automáticamente " +
                    "en las cookies de respuesta para mantener la sesión activa sin intervención del cliente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados exitosamente. Las cookies se actualizan con los nuevos valores",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token no encontrado en las cookies, expirado o inválido",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromRequest(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Refresh token not found in cookies"));
        }

        try {
            Map<String, Object> tokens = refreshTokenService.refreshTokens(refreshToken);
            cookieService.setAccessTokenCookie(response, (String) tokens.get("access_token"));
            cookieService.setRefreshTokenCookie(response, (String) tokens.get("refresh_token"));
            return ResponseEntity.ok(Map.of("message", "Tokens refreshed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Finaliza la sesión del usuario eliminando todas las cookies de autenticación " +
                    "(access token y refresh token) del navegador. Después de esta operación, " +
                    "el cliente deberá volver a autenticarse para acceder a los recursos protegidos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente. Las cookies de autenticación han sido eliminadas",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {
        cookieService.clearAllTokenCookies(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Operation(
            summary = "Iniciar registro de paciente",
            description = "Inicia el proceso de registro de un nuevo paciente en el sistema. " +
                    "Este endpoint recibe los datos básicos del usuario (ID de Google, email, nombre y fecha de nacimiento) " +
                    "y crea un registro pendiente que requerirá pasos adicionales como la aceptación de consentimientos " +
                    "informados y la compleción del perfil médico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proceso de registro iniciado exitosamente. Retorna los datos del registro pendiente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Causas: email con formato incorrecto, " +
                            "fecha de nacimiento futura o campos requeridos ausentes",
                    content = @Content
            )
    })
    @PostMapping("/init-registration")
    public ResponseEntity<?> initRegistration(
            @Valid @RequestBody
            @Parameter(description = "Datos básicos para iniciar el registro del paciente", required = true)
            InitRegistrationRequest request) {
        try {
            var resp = registrationService.initRegistration(request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Enviar consentimiento informado",
            description = "Registra la aceptación de los consentimientos informados por parte del paciente " +
                    "durante el proceso de registro. Este endpoint requiere el token JWT que contiene " +
                    "el identificador del registro pendiente (pendingUserConfigId)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Consentimiento registrado exitosamente. El perfil del paciente avanza al siguiente paso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se encontró un registro pendiente en el token o el token no contiene la información necesaria",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autorización ausente, mal formado o inválido",
                    content = @Content
            )
    })
    @PostMapping("/submit-consent")
    public ResponseEntity<?> submitConsent(
            @Parameter(description = "Header de autorización con el token JWT en formato 'Bearer {token}'. " +
                    "El token debe contener el pendingUserConfigId del registro en curso",
                    required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            String pendingUserConfigId = jwtTokenService.getPendingUserConfigId(token);

            if (pendingUserConfigId == null || pendingUserConfigId.isBlank()) {
                return ResponseEntity.status(400).body(Map.of("error", "No pending registration found in token"));
            }

            var resp = registrationService.submitConsent(pendingUserConfigId);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}