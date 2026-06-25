package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.CompleteProfileRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientResponse;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/patient")
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

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CompleteProfileRequest request,
            HttpServletRequest httpRequest) {
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

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getCurrentPatient(Authentication authentication) {
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
