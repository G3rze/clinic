package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.ConsentRequest;
import com.terraplanistas.clinic.domain.dto.request.PatientProfileRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientProfileResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.services.PatientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/auth/profile")
@Tag(name = "Perfil del Paciente",
        description = "Endpoints para la gestión del perfil personal del paciente autenticado. " +
                "Permite consultar y actualizar datos personales, gestionar consentimientos informados " +
                "y verificar el estado de completitud del perfil.")
@SecurityRequirement(name = "bearerAuth")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @Operation(
            summary = "Consultar perfil del paciente",
            description = "Recupera la información completa del perfil del paciente autenticado, " +
                    "incluyendo indicadores de estado: si el perfil está completo, si es mayor de edad " +
                    "y si ha otorgado los consentimientos informados requeridos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil del paciente recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Perfil de paciente no encontrado para el usuario actual", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PatientProfileResponse> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientProfileService.getPatientByUserId(userId);
        boolean isComplete = patientProfileService.isProfileComplete(userId);
        boolean isAdult = patientProfileService.isPatientAdult(userId);
        boolean hasConsent = patientProfileService.isConsentGiven(userId);

        return ResponseEntity.ok(PatientProfileResponse.from(patient, isComplete, isAdult, hasConsent));
    }

    @Operation(
            summary = "Actualizar perfil del paciente",
            description = "Modifica los datos personales del paciente autenticado. " +
                    "Una vez actualizados todos los campos requeridos, el perfil se marca como completo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente. Retorna el perfil completo con estado actualizado",
                    content = @Content(schema = @Schema(implementation = PatientProfileResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos. Documento con formato incorrecto o fecha futura", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "409", description = "El número de documento ya está registrado para otro paciente", content = @Content)
    })
    @PutMapping
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Nuevos datos del perfil del paciente", required = true)
            PatientProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientProfileService.updateProfile(userId, request);
        boolean isAdult = patientProfileService.isPatientAdult(userId);
        boolean hasConsent = patientProfileService.isConsentGiven(userId);

        return ResponseEntity.ok(PatientProfileResponse.from(patient, true, isAdult, hasConsent));
    }

    @Operation(
            summary = "Consultar versión del consentimiento",
            description = "Obtiene la versión actual del documento de consentimiento informado " +
                    "que el paciente debe aceptar. Útil para detectar cuando se requiere re-consentimiento " +
                    "por actualización de términos y condiciones."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Versión actual del consentimiento recuperada exitosamente",
                    content = @Content
            )
    })
    @GetMapping("/consent/version")
    public ResponseEntity<Map<String, String>> getConsentVersion() {
        return ResponseEntity.ok(Map.of("version", patientProfileService.getCurrentConsentVersion()));
    }

    @Operation(
            summary = "Otorgar consentimiento informado",
            description = "Registra la aceptación de los consentimientos informados por parte del paciente. " +
                    "El paciente debe aceptar tanto el consentimiento para tratamiento médico " +
                    "como el consentimiento para análisis de datos con fines estadísticos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consentimientos registrados exitosamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos. Ambos consentimientos deben ser especificados", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @PostMapping("/consent")
    public ResponseEntity<Void> giveConsent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Valores de aceptación para cada tipo de consentimiento", required = true)
            ConsentRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        patientProfileService.recordConsent(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Consultar estado de consentimientos",
            description = "Verifica si el paciente autenticado ya ha otorgado los consentimientos informados " +
                    "y retorna la versión actual del documento de consentimiento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de consentimientos consultado exitosamente",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @GetMapping("/consent/status")
    public ResponseEntity<Map<String, Object>> getConsentStatus(
            @Parameter(hidden = true) Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean hasConsent = patientProfileService.isConsentGiven(userId);
        String version = patientProfileService.getCurrentConsentVersion();

        return ResponseEntity.ok(Map.of(
                "hasConsent", hasConsent,
                "version", version
        ));
    }
}