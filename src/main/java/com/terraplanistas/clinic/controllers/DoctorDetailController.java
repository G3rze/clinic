package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AddAvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.request.UpdateEmployeeSpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.domain.dto.response.DoctorDetailResponse;
import com.terraplanistas.clinic.domain.dto.response.EmployeeSpecialtyResponse;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import com.terraplanistas.clinic.services.EmployeeSpecialtyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class DoctorDetailController {

    private final EmployeeSpecialtyService employeeSpecialtyService;
    private final DoctorAvailabilityService availabilityService;

    public DoctorDetailController(EmployeeSpecialtyService employeeSpecialtyService,
                                  DoctorAvailabilityService availabilityService) {
        this.employeeSpecialtyService = employeeSpecialtyService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<DoctorDetailResponse> getDoctorDetail(@PathVariable UUID id) {
        DoctorDetailResponse detail = employeeSpecialtyService.getDoctorDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/specialties")
    public ResponseEntity<List<EmployeeSpecialtyResponse>> getDoctorSpecialties(@PathVariable UUID id) {
        List<EmployeeSpecialtyResponse> specialties = employeeSpecialtyService.getSpecialtiesByEmployeeId(id);
        return ResponseEntity.ok(specialties);
    }

    @PostMapping("/{id}/specialties")
    public ResponseEntity<EmployeeSpecialtyResponse> addSpecialty(
            @PathVariable UUID id,
            @RequestBody AddSpecialtyRequest request) {
        EmployeeSpecialtyResponse response = employeeSpecialtyService.addSpecialtyToEmployee(
                id, request.specialtyId(), request.professionalLicenseNumber());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{employeeId}/specialties/{specialtyId}")
    public ResponseEntity<EmployeeSpecialtyResponse> updateSpecialty(
            @PathVariable UUID employeeId,
            @PathVariable UUID specialtyId,
            @Valid @RequestBody UpdateEmployeeSpecialtyRequest request) {
        EmployeeSpecialtyResponse response = employeeSpecialtyService.updateEmployeeSpecialty(
                employeeId, specialtyId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employeeId}/specialties/{specialtyId}")
    public ResponseEntity<Void> removeSpecialty(
            @PathVariable UUID employeeId,
            @PathVariable UUID specialtyId) {
        employeeSpecialtyService.removeSpecialtyFromEmployee(employeeId, specialtyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{employeeId}/specialties/{specialtyId}/availability")
    public ResponseEntity<AvailabilityResponse> addAvailability(
            @PathVariable UUID employeeId,
            @PathVariable UUID specialtyId,
            @Valid @RequestBody AddAvailabilityRequest request,
            Authentication authentication) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        AvailabilityResponse response = availabilityService.createAvailability(
                employeeId, specialtyId, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    public record AddSpecialtyRequest(
            UUID specialtyId,
            String professionalLicenseNumber
    ) {}
}
