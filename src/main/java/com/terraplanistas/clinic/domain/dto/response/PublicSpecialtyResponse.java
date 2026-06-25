package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PublicSpecialtyResponse(
    UUID specialtyId,
    String code,
    String name,
    String professionalLicenseNumber,
    BigDecimal feePerHour,
    Integer consultDurationMinutes,
    List<PublicAvailabilityResponse> availability
) {}