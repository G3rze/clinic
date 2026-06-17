package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.services.AppointmentRatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentRatingController {

    private final AppointmentRatingService ratingService;

    public AppointmentRatingController(AppointmentRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PatchMapping("/{appointmentId}/rating")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rateAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody RateAppointmentRequest request) {
        AppointmentResponse response = ratingService.rateAppointment(appointmentId, request.patientId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
