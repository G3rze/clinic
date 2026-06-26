package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.PrescriptionRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.PrescriptionResponse;
import com.terraplanistas.clinic.services.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * JC1  POST   /api/v1/prescriptions                              — doctor crea receta
 * JC2  PATCH  /api/v1/prescriptions/{id}/dispense               — dispensar (max 3 usos)
 * JC3  GET    /api/v1/prescriptions/patient/{patientId}         — paciente ve sus recetas
 * JC6  GET    /api/v1/prescriptions/{id}                        — ver/descargar receta individual
 *       GET    /api/v1/prescriptions/appointment/{appointmentId} — recetas de una cita
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /** JC1 — Doctor crea receta. doctorUserId viene del header X-User-Id (JWT del doctor). */
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody PrescriptionRequest request,
            @RequestHeader("X-User-Id") UUID doctorUserId) {

        PrescriptionResponse body = prescriptionService.createPrescription(request, doctorUserId);
        URI location = URI.create("/api/v1/prescriptions/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    /** JC2 — Dispensar receta (farmacia incrementa usageCount) */
    @PatchMapping("/{id}/dispense")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> dispense(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.dispensePrescription(id)));
    }

    /** JC3 / JC6 — Paciente ve todas sus recetas */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByPatient(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByPatient(patientId)));
    }

    /** JC6 — Ver / descargar receta individual */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionById(id)));
    }

    /** Recetas de una cita */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByAppointment(
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByAppointment(appointmentId)));
    }
}
