package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record EmployeeSpecialtyRequest(
    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotNull(message = "Specialty ID is required")
    UUID specialtyId,

    @NotBlank(message = "Professional license number is required")
    String professionalLicenseNumber,

    @NotNull(message = "Fee per hour is required")
    @DecimalMin(value = "0.01", message = "Fee per hour must be at least 0.01")
    BigDecimal feePerHour,

    @NotNull(message = "Shift is required")
    Map<String, Object> shift,

    @Min(value = 5, message = "Consult duration must be at least 5 minutes")
    Integer consultDurationMinutes
) {}