package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentRequest(
    @NotBlank(message = "Google Event ID is required")
    String googleEventId,

    @NotNull(message = "Status is required")
    AppointmentStatus status,

    @NotNull(message = "Fee per hour is required")
    @DecimalMin(value = "0.01", message = "Fee per hour must be greater than 0")
    BigDecimal finalFeePerHour,

    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 10, message = "Score cannot be greater than 10")
    Integer score,

    String review,

    @NotNull(message = "Registered date is required")
    OffsetDateTime registeredAt,

    @NotNull(message = "Expected date is required")
    @Future(message = "Expected date must be in the future")
    OffsetDateTime expectedAt,

    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Patient Caller User ID is required")
    UUID patientCallerUserId
) {}