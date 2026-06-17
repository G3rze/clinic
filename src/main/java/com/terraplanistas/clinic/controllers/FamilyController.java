package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AddDependentRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.services.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping("/dependents")
    public ResponseEntity<PatientResponse> addDependent(Authentication authentication,
                                                         @Valid @RequestBody AddDependentRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        Patient dependent = familyService.addDependent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PatientResponse.from(dependent));
    }

    @GetMapping("/dependents")
    public ResponseEntity<List<PatientResponse>> getDependents(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Patient> dependents = familyService.getDependents(userId);
        List<PatientResponse> response = dependents.stream()
                .map(PatientResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dependents/{id}")
    public ResponseEntity<PatientResponse> getDependentById(Authentication authentication,
                                                             @PathVariable UUID id) {
        UUID userId = (UUID) authentication.getPrincipal();
        if (!familyService.isUserRepresentativeOf(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        Patient dependent = familyService.getDependentById(id);
        return ResponseEntity.ok(PatientResponse.from(dependent));
    }

    @DeleteMapping("/dependents/{id}")
    public ResponseEntity<Void> removeDependent(Authentication authentication,
                                                 @PathVariable UUID id) {
        UUID userId = (UUID) authentication.getPrincipal();
        if (!familyService.isUserRepresentativeOf(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        familyService.removeDependent(id, userId);
        return ResponseEntity.noContent().build();
    }
}
