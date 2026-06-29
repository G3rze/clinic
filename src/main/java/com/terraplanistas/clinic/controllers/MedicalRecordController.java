package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService service;
    private final PatientRepository patientRepository;

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> registerRecord(Authentication authentication, @RequestBody @Valid MedicalRecordRequest request){
        UUID doctor = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.registerRecord(doctor, request)
        );
    }


    @GetMapping("/appointment/{id}")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getFromAppointment(Authentication authentication, @PathVariable UUID id){
        UUID requester = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(service.getFromAppointmentId(requester, id)));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getByPatient(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @RequestParam(required = false) UUID doctorId) {

        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new com.terraplanistas.clinic.exceptions.ResourceNotFoundException("Patient not found for current user"));

        List<MedicalRecordResponse> records;
        if (fromDate != null || toDate != null || doctorId != null) {
            records = service.getByPatientIdAndFilters(patient.getId(), fromDate, toDate, doctorId);
        } else {
            records = service.getByPatientId(patient.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
