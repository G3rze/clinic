package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Patient Caller User ID is required")
    UUID patientCallerUserId,

    @NotBlank(message = "Specialty code is required")
    String specialtyCode,

    @NotNull(message = "Expected date is required")
    OffsetDateTime expectedAt
) {}
