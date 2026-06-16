package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.util.UUID;

public record AvailabilityRequest(
    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotNull(message = "Specialty ID is required")
    UUID specialtyId,

    @NotNull(message = "Day of week is required")
    DayOfWeek dayOfWeek,

    @NotNull(message = "Start time is required")
    OffsetTime startTime,

    @NotNull(message = "End time is required")
    OffsetTime endTime
) {}
