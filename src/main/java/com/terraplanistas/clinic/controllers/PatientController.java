package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.CompleteProfileRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientResponse;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/patient")
@Tag(name = "Registro de Paciente",
        description = "Endpoints para la finalización del proceso de registro de nuevos pacientes. " +
                "Permite completar el perfil con datos personales y consultar el perfil del paciente actual.")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final RegistrationService registrationService;
    private final JwtTokenService jwtTokenService;
    private final PatientRepository patientRepository;

    public PatientController(RegistrationService registrationService,
                             JwtTokenService jwtTokenService,
                             PatientRepository patientRepository) {
        this.registrationService = registrationService;
        this.jwtTokenService = jwtTokenService;
        this.patientRepository = patientRepository;
    }

    @Operation(
            summary = "Completar perfil del paciente",
            description = "Finaliza el proceso de registro del paciente completando sus datos personales. " +
                    "Este endpoint se utiliza durante el flujo de registro cuando el paciente " +
                    "debe proporcionar su información de identidad, dirección y contacto. " +
                    "Requiere un token JWT que contenga el identificador del registro pendiente (pendingUserConfigId)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil completado exitosamente. El paciente ya puede acceder a todas las funcionalidades",
                    content = @Content
            ),
            @ApiResponse(responseCode = "400", description = "No se encontró registro pendiente en el token o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autorización ausente, mal formado o inválido", content = @Content),
            @ApiResponse(responseCode = "409", description = "El número de documento ya está registrado en el sistema", content = @Content)
    })
    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @Parameter(description = "Header de autorización con token JWT en formato 'Bearer {token}'. " +
                    "El token debe contener el pendingUserConfigId", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody
            @Parameter(description = "Datos personales para completar el perfil del paciente", required = true)
            CompleteProfileRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            String pendingUserConfigId = jwtTokenService.getPendingUserConfigId(token);

            if (pendingUserConfigId == null || pendingUserConfigId.isBlank()) {
                return ResponseEntity.status(400).body(Map.of("error", "No pending registration found in token"));
            }

            String ipAddress = httpRequest.getRemoteAddr();
            String responseJson = registrationService.completeProfile(pendingUserConfigId, request, ipAddress);
            return ResponseEntity.ok(Map.of("message", responseJson));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Consultar perfil del paciente actual",
            description = "Recupera la información del perfil del paciente asociado al usuario autenticado. " +
                    "Retorna los datos básicos del paciente activo vinculado al token JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil del paciente recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontró perfil de paciente activo para el usuario actual", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getCurrentPatient(
            @Parameter(hidden = true) Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        PatientResponse patient = patientRepository
                .findByUserIdAndIsActiveTrue(userId)
                .map(PatientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for user ID: " + userId
                ));

        return ResponseEntity.ok(patient);
    }
}