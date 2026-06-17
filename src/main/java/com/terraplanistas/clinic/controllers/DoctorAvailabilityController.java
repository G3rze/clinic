package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availability")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AvailabilityResponse>> createAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.createAvailability(request);
        URI location = URI.create("/api/v1/availability/" + response.id());
        return ResponseEntity.created(location).body(ApiResponse.created(response, location.toString()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.updateAvailability(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable UUID id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(ApiResponse.success((Void) null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(@PathVariable UUID id) {
        AvailabilityResponse response = availabilityService.getAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailabilities(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) UUID specialtyId) {
        List<AvailabilityResponse> response = specialtyId != null
            ? availabilityService.getAvailabilitiesByEmployeeAndSpecialty(employeeId, specialtyId)
            : availabilityService.getAvailabilitiesByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
