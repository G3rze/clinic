package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.LaboratoryRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.LaboratoryResponse;
import com.terraplanistas.clinic.services.LaboratoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LaboratoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(laboratoryService.getAllLaboratories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(laboratoryService.getLaboratoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> create(
            Authentication authentication,
            @Valid @RequestBody LaboratoryRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        LaboratoryResponse body = laboratoryService.createLaboratory(request.name(), currentUserId);
        URI location = URI.create("/api/v1/laboratories/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody LaboratoryRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(
                laboratoryService.updateLaboratory(id, request.name(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        laboratoryService.deleteLaboratory(id);
        return ResponseEntity.noContent().build();
    }
}