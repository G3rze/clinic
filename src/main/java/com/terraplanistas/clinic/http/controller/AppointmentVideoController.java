package com.terraplanistas.clinic.http.controller;

import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.services.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints de video / Meet para citas.
 *
 * JC4  POST  /api/v1/appointments/{id}/meet         — genera enlace Google Meet
 * JC5  POST  /api/v1/appointments/{id}/join         — paciente confirma que se unió
 */
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentVideoController {

    private final AppointmentService appointmentService;

    public AppointmentVideoController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * JC4 — El doctor (o sistema) genera el enlace Meet para la cita.
     * googleUserId: ID de Google del doctor autenticado (viene del header o sesión).
     */
    @PostMapping("/{id}/meet")
    public ResponseEntity<ApiResponse<AppointmentResponse>> generateMeetLink(
            @PathVariable UUID id,
            @RequestHeader("X-Google-User-Id") String googleUserId) {

        AppointmentResponse response = appointmentService.generateMeetLink(id, googleUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * JC5 — El paciente confirma que se unió a la videoconsulta.
     * Registra patientJoinedAt y cambia estado a IN_PROGRESS.
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmPatientJoined(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID patientId) {

        AppointmentResponse response = appointmentService.confirmPatientJoined(id, patientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** Consultar cita (incluye meetLink y patientJoinedAt) */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
