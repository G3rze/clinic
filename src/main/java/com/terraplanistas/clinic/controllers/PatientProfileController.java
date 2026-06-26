package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.ConsentRequest;
import com.terraplanistas.clinic.domain.dto.request.PatientProfileRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientProfileResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.services.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/auth/profile")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping
    public ResponseEntity<PatientProfileResponse> getProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientProfileService.getPatientByUserId(userId);
        boolean isComplete = patientProfileService.isProfileComplete(userId);
        boolean isAdult = patientProfileService.isPatientAdult(userId);
        boolean hasConsent = patientProfileService.isConsentGiven(userId);

        return ResponseEntity.ok(PatientProfileResponse.from(patient, isComplete, isAdult, hasConsent));
    }

    @PutMapping
    public ResponseEntity<PatientProfileResponse> updateProfile(Authentication authentication,
                                                                 @Valid @RequestBody PatientProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientProfileService.updateProfile(userId, request);
        boolean isAdult = patientProfileService.isPatientAdult(userId);
        boolean hasConsent = patientProfileService.isConsentGiven(userId);

        return ResponseEntity.ok(PatientProfileResponse.from(patient, true, isAdult, hasConsent));
    }

    @GetMapping("/consent/version")
    public ResponseEntity<Map<String, String>> getConsentVersion() {
        return ResponseEntity.ok(Map.of("version", patientProfileService.getCurrentConsentVersion()));
    }

    @PostMapping("/consent")
    public ResponseEntity<Void> giveConsent(Authentication authentication,
                                             @Valid @RequestBody ConsentRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        patientProfileService.recordConsent(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/consent/status")
    public ResponseEntity<Map<String, Object>> getConsentStatus(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean hasConsent = patientProfileService.isConsentGiven(userId);
        String version = patientProfileService.getCurrentConsentVersion();

        return ResponseEntity.ok(Map.of(
                "hasConsent", hasConsent,
                "version", version
        ));
    }
}
