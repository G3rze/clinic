package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public record AvailableSlotsQuery(
    @NotNull(message = "Doctor ID is required")
    UUID doctorId,

    @NotNull(message = "Specialty code is required")
    String specialtyCode,

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    LocalDate endDate,

    ZoneId timezone,

    @NotNull(message = "Consult duration is required")
    @Min(value = 5, message = "Consult duration must be at least 5 minutes")
    Integer consultDurationMinutes
) {}
