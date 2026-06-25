package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.entities.Specialty;
import com.terraplanistas.clinic.services.SpecialtyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/specialty")
@PreAuthorize("hasRole('ADMIN')")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public ResponseEntity<List<Specialty>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specialty> getSpecialtyById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(id));
    }

    @PostMapping
    public ResponseEntity<Specialty> createSpecialty(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String code = request.get("code");
        String name = request.get("name");
        UUID currentUserId = UUID.fromString(authentication.getName());
        Specialty specialty = specialtyService.createSpecialty(code, name, currentUserId);
        return ResponseEntity.ok(specialty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable UUID id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.noContent().build();
    }
}