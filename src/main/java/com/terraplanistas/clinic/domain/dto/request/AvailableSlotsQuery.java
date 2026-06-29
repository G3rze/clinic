package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Schema(description = "DTO de consulta para buscar franjas horarias disponibles de un médico. " +
        "Permite especificar el rango de fechas, especialidad médica y duración de la consulta deseada")
public record AvailableSlotsQuery(
        @NotNull(message = "Doctor ID is required")
        @Schema(description = "UUID del médico para el cual se buscan las franjas horarias disponibles",
                example = "660e8400-e29b-41d4-a716-446655440001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID doctorId,

        @NotNull(message = "Specialty code is required")
        @Schema(description = "Código de la especialidad médica para filtrar la disponibilidad. " +
                "Ejemplos: 'CARDIOLOGY', 'PEDIATRICS', 'DERMATOLOGY'",
                example = "CARDIOLOGY",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String specialtyCode,

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        @Schema(description = "Fecha de inicio del rango de búsqueda de slots disponibles. " +
                "Debe ser una fecha futura en formato ISO (YYYY-MM-DD)",
                example = "2026-07-01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        @Schema(description = "Fecha de fin del rango de búsqueda de slots disponibles. " +
                "Debe ser una fecha futura y posterior o igual a startDate",
                example = "2026-07-07",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endDate,

        @Schema(description = "Zona horaria para el cálculo de las franjas horarias. " +
                "Si no se especifica, se utiliza la zona horaria del servidor. " +
                "Ejemplos: 'America/Bogota', 'America/Mexico_City', 'Europe/Madrid'",
                example = "America/Bogota",
                nullable = true)
        ZoneId timezone,

        @Min(value = 5, message = "Consult duration must be at least 5 minutes")
        @Schema(description = "Duración estimada de la consulta en minutos. " +
                "Determina el tamaño de las franjas horarias a buscar. Mínimo 5 minutos",
                example = "30",
                defaultValue = "30",
                minimum = "5")
        Integer consultDurationMinutes
) {}