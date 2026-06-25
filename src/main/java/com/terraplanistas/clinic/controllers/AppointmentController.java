package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.ConfirmPaymentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentTransactionResponse;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.services.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/appointments")
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

        UUID userId = UUID.fromString(authentication.getName());

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

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countAppointments(
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            long count = appointmentService.countAppointmentsForDate(localDate);
            return ResponseEntity.ok(Map.of("date", date, "count", count));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid date format. Use YYYY-MM-DD (e.g., 2026-06-22)"
            ));
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<AppointmentTransactionResponse> createAppointment(
            @Valid @RequestBody AppointmentTransactionRequest request
    ) {

        return ResponseEntity.ok(
                appointmentService.createAppointmentWithPayment(
                        request
                )
        );
    }

    @PostMapping("/{appointmentId}/confirm-payment")
    public ResponseEntity<AppointmentResponse> confirmPayment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody ConfirmPaymentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.confirmPayment(appointmentId, request.paymentIntentId())
        );
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable UUID appointmentId
    ) {

        appointmentService.cancelAppointment(
                appointmentId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointment(appointmentId)
        );
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

}
