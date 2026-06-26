package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.services.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService service;

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> registerRecord(Authentication authentication, @RequestBody @Valid MedicalRecordRequest request){
        UUID doctor = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.registerRecord(doctor, request)
        );
    }


    @GetMapping("/appointment/{id}")
    public ResponseEntity<List<MedicalRecordResponse>> getFromAppointment(Authentication authentication, @PathVariable UUID id){
        UUID requester = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(

                service.getFromAppointmentId(requester
                        ,id)
        );
    }
}
