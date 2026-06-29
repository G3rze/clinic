package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "DTO para actualizar la configuración de una especialidad asignada a un médico. " +
        "Permite modificar la licencia profesional, tarifa por hora y duración de consulta")
public record UpdateEmployeeSpecialtyRequest(
        @NotBlank(message = "Professional license number is required")
        @Schema(description = "Número de licencia profesional del médico para la especialidad. " +
                "Identificador oficial emitido por el organismo regulador correspondiente",
                example = "CAR-2020-12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String professionalLicenseNumber,

        @NotNull(message = "Fee per hour is required")
        @DecimalMin(value = "1.0", message = "Minimum fee is $1")
        @Schema(description = "Tarifa por hora de consulta para esta especialidad en la moneda configurada. " +
                "Valor mínimo: 1.00",
                example = "150.00",
                minimum = "1.0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal feePerHour,

        @NotNull(message = "Consult duration is required")
        @Min(value = 15, message = "Minimum duration is 15 minutes")
        @Schema(description = "Duración estándar de la consulta en minutos para esta especialidad. " +
                "Mínimo 15 minutos",
                example = "30",
                minimum = "15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer consultDurationMinutes
) {}