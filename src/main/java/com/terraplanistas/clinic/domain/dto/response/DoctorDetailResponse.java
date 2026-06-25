package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

public record DoctorDetailResponse(
    UUID id,
    UUID userId,
    String firstName,
    String lastName,
    String email,
    String roleCode,
    String status,
    String phones,
    String address,
    List<SpecialtyDetail> specialties
) {
    public record SpecialtyDetail(
        UUID specialtyId,
        String code,
        String name,
        String professionalLicenseNumber,
        BigDecimal feePerHour,
        Integer consultDurationMinutes,
        List<AvailabilitySlot> availabilities
    ) {}

    public record AvailabilitySlot(
        UUID id,
        DayOfWeek dayOfWeek,
        OffsetTime startTime,
        OffsetTime endTime
    ) {}
}
