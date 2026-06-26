package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.PublicDoctorResponse;
import com.terraplanistas.clinic.services.EmployeeSpecialtyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/doctors")
public class PublicDoctorController {

    private final EmployeeSpecialtyService employeeSpecialtyService;

    public PublicDoctorController(EmployeeSpecialtyService employeeSpecialtyService) {
        this.employeeSpecialtyService = employeeSpecialtyService;
    }

    @GetMapping
    public ResponseEntity<Page<PublicDoctorResponse>> getDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PublicDoctorResponse> doctors = employeeSpecialtyService.getPublicDoctors(
                name, specialty, PageRequest.of(page, size));
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicDoctorResponse> getDoctorDetail(@PathVariable UUID id) {
        PublicDoctorResponse doctor = employeeSpecialtyService.getPublicDoctorDetail(id);
        return ResponseEntity.ok(doctor);
    }
}