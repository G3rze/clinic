package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.ConfirmPaymentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentTransactionResponse;
import com.terraplanistas.clinic.domain.dto.response.CheckoutSessionResponse;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.http.config.AppStripeProperties;
import com.terraplanistas.clinic.http.stripe.StripePaymentService;
import com.terraplanistas.clinic.services.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final StripePaymentService stripePaymentService;
    private final AppStripeProperties appStripeProperties;

    public AppointmentController(AppointmentService appointmentService, StripePaymentService stripePaymentService,
                                 AppStripeProperties appStripeProperties) {
        this.appointmentService = appointmentService;
        this.stripePaymentService = stripePaymentService;
        this.appStripeProperties = appStripeProperties;
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(
            Authentication authentication,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) AppointmentStatus status) {

        UUID userId = UUID.fromString(authentication.getName());

        YearMonth yearMonth = null;
        if (month != null && !month.isBlank()) {
            try {
                yearMonth = YearMonth.parse(month);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid month format. Use YYYY-MM (e.g., 2026-06)"
                ));
            }
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
            @Valid @RequestBody AppointmentTransactionRequest request,
            @RequestParam(required = false) UUID existingAppointmentId
    ) {
        if (existingAppointmentId != null) {
            return ResponseEntity.ok(
                    appointmentService.retryPaymentForExistingAppointment(existingAppointmentId)
            );
        }
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

    @GetMapping("/{appointmentId}/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @PathVariable UUID appointmentId,
            @RequestParam String successUrl,
            @RequestParam String cancelUrl
    ) {
        return ResponseEntity.ok(
                appointmentService.createCheckoutSession(appointmentId, successUrl, cancelUrl)
        );
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout-session/{sessionId}/confirm")
    public ResponseEntity<AppointmentResponse> confirmCheckoutSession(
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(
                appointmentService.confirmCheckoutSession(sessionId)
        );
    }

    @GetMapping("/checkout-session/{sessionId}/info")
    public ResponseEntity<?> getCheckoutSessionInfo(@PathVariable String sessionId) {
        StripePaymentService.CheckoutSessionInfo info = stripePaymentService.retrieveCheckoutSession(sessionId);
        return ResponseEntity.ok(Map.of(
            "sessionId", info.sessionId(),
            "appointmentId", info.appointmentId() != null ? info.appointmentId().toString() : null,
            "status", info.status()
        ));
    }

    @GetMapping("/features")
    public ResponseEntity<Map<String, Boolean>> getFeatures() {
        return ResponseEntity.ok(Map.of("stripeEnabled", appStripeProperties.isEnabled()));
    }

    @PostMapping("/{appointmentId}/confirm-simulate")
    public ResponseEntity<AppointmentResponse> confirmSimulatePayment(@PathVariable UUID appointmentId) {
        log.info("Simulation payment confirm requested for appointment {}", appointmentId);
        return ResponseEntity.ok(appointmentService.confirmSimulatePayment(appointmentId));
    }

}
