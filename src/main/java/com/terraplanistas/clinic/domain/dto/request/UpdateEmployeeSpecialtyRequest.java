package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateEmployeeSpecialtyRequest(
    @NotBlank(message = "Professional license number is required")
    String professionalLicenseNumber,

    @NotNull(message = "Fee per hour is required")
    @DecimalMin(value = "1.0", message = "Minimum fee is $1")
    BigDecimal feePerHour,

    @NotNull(message = "Consult duration is required")
    @Min(value = 15, message = "Minimum duration is 15 minutes")
    Integer consultDurationMinutes
) {}
