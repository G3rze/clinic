package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record EmployeeSpecialtyResponse(
    UUID employeeId,
    UUID specialtyId,
    String professionalLicenseNumber,
    BigDecimal feePerHour,
    Map<String, Object> shift,
    UUID id
) {}