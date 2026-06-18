package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.services.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(
            Authentication authentication,
            @RequestParam String month,
            @RequestParam(required = false) AppointmentStatus status) {

        UUID userId = (UUID) authentication.getPrincipal();

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid month format. Use YYYY-MM (e.g., 2026-06)"
            ));
        }

        Page<AppointmentResponse> appointments = appointmentService.getAppointmentsForUser(userId, yearMonth, status);

        return ResponseEntity.ok(appointments);
    }
}
