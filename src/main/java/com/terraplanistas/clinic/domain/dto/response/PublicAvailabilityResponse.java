package com.terraplanistas.clinic.domain.dto.response;

import java.time.DayOfWeek;

/**
 * Public availability response for patient-facing endpoints.
 * Times are returned in full ISO 8601 format with clinic timezone offset.
 * Example: "2024-06-15T08:00:00-06:00"
 */
public record PublicAvailabilityResponse(
    DayOfWeek dayOfWeek,
    String startTime,
    String endTime
) {}