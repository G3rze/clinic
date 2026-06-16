package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RateAppointmentRequest(
    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    Integer score,

    @Size(max = 1000, message = "Review cannot exceed 1000 characters")
    String review
) {}
