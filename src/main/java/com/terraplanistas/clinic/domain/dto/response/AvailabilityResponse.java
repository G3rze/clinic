package com.terraplanistas.clinic.domain.dto.response;

import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.util.UUID;

public record AvailabilityResponse(
    UUID id,
    UUID employeeId,
    UUID specialtyId,
    DayOfWeek dayOfWeek,
    OffsetTime startTime,
    OffsetTime endTime,
    Boolean isActive
) {}
