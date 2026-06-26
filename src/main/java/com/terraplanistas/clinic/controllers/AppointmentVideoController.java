package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.mapper.AppointmentMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.http.google.GoogleEventsService;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JC4  POST /api/v1/appointments/{id}/meet  — genera enlace Google Meet
 * JC5  POST /api/v1/appointments/{id}/join  — paciente confirma que se unió
 */
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentVideoController {

    private final AppointmentRepository appointmentRepository;
    private final GoogleEventsService googleEventsService;

    public AppointmentVideoController(AppointmentRepository appointmentRepository,
                                      GoogleEventsService googleEventsService) {
        this.appointmentRepository = appointmentRepository;
        this.googleEventsService = googleEventsService;
    }

    // ------------------------------------------------------------------
    // JC4 — Generar enlace de Google Meet para la cita
    // ------------------------------------------------------------------
    @PostMapping("/{id}/meet")
    public ResponseEntity<ApiResponse<AppointmentResponse>> generateMeetLink(
            @PathVariable UUID id,
            @RequestHeader("X-Google-User-Id") String googleUserId) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Cannot generate a Meet link for a cancelled or completed appointment");
        }

        // Idempotente: si ya tiene link no lo regeneramos
        if (appointment.getMeetLink() != null && !appointment.getMeetLink().isBlank()) {
            var eventInfo = fetchEventInfo(appointment, googleUserId);
            return ResponseEntity.ok(ApiResponse.success(AppointmentMapper.toResponse(appointment, eventInfo)));
        }

        // El meet link ya fue creado por Marco en createAppointmentWithPayment().
        // Solo necesitamos leerlo del evento de Google Calendar y persistirlo.
        var googleEvent = googleEventsService.getEventObject(
                googleUserId, "primary", appointment.getGoogleEventId());

        var eventInfo = AppointmentMapper.toGoogleEventInfoResponse(googleEvent);
        String meetLink = eventInfo != null ? eventInfo.meetLink() : null;

        if (meetLink == null) {
            throw new BusinessRuleException(
                    "Google Meet link is not available yet for this event. " +
                    "Make sure the Google Calendar event was created with conferenceData.");
        }

        appointment.setMeetLink(meetLink);
        appointmentRepository.save(appointment);

        return ResponseEntity.ok(ApiResponse.success(AppointmentMapper.toResponse(appointment, eventInfo)));
    }

    // ------------------------------------------------------------------
    // JC5 — Paciente confirma que se unió a la videoconsulta
    // ------------------------------------------------------------------
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmPatientJoined(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID patientId) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Validar que el paciente pertenece a esta cita
        if (appointment.getPatient() == null
                || !appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessRuleException("Patient does not belong to this appointment");
        }

        // Validar que el link de Meet ya existe
        if (appointment.getMeetLink() == null || appointment.getMeetLink().isBlank()) {
            throw new BusinessRuleException("This appointment does not have a Meet link yet");
        }

        // No se puede unir a una cita cancelada o completada
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot join a cancelled or completed appointment");
        }

        // Solo persiste la primera vez
        if (appointment.getPatientJoinedAt() == null) {
            appointment.setPatientJoinedAt(OffsetDateTime.now());
        }

        // SCHEDULED → IN_PROGRESS al unirse el paciente
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        }

        appointmentRepository.save(appointment);

        return ResponseEntity.ok(ApiResponse.success(
                AppointmentMapper.toResponse(appointment, null)));
    }

    // ------------------------------------------------------------------
    // Helper: obtiene el eventInfo de Google sin lanzar si falla
    // ------------------------------------------------------------------
    private com.terraplanistas.clinic.domain.dto.response.GoogleEventInfoResponse fetchEventInfo(
            Appointment appointment, String googleUserId) {
        try {
            var event = googleEventsService.getEventObject(
                    googleUserId, "primary", appointment.getGoogleEventId());
            return AppointmentMapper.toGoogleEventInfoResponse(event);
        } catch (Exception e) {
            return null;
        }
    }
}
