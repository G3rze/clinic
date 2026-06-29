package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.MedicineRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.MedicineResponse;
import com.terraplanistas.clinic.services.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<MedicineResponse> medicines = medicineService.searchMedicines(search, page, size);
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getMedicineById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(
            Authentication authentication,
            @Valid @RequestBody MedicineRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        MedicineResponse body = medicineService.createMedicine(request, currentUserId);
        URI location = URI.create("/api/v1/medicines/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody MedicineRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(
                medicineService.updateMedicine(id, request, currentUserId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}