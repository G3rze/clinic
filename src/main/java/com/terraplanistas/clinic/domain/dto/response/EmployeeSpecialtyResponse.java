package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO con la información de una especialidad asignada a un empleado (médico)")
public record EmployeeSpecialtyResponse(
        @Schema(description = "UUID del empleado (médico)",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID employeeId,

        @Schema(description = "UUID de la especialidad médica",
                example = "880e8400-e29b-41d4-a716-446655440003")
        UUID specialtyId,

        @Schema(description = "Número de licencia profesional del médico para esta especialidad",
                example = "CAR-2020-12345")
        String professionalLicenseNumber,

        @Schema(description = "Tarifa por hora de consulta en la moneda configurada",
                example = "200.00")
        BigDecimal feePerHour,

        @Schema(description = "Información del turno o jornada laboral configurada para esta especialidad. " +
                "Contiene los días y horarios de atención",
                example = "{\"MONDAY\": {\"start\": \"08:00\", \"end\": \"17:00\"}}")
        Map<String, Object> shift,

        @Schema(description = "Duración estándar de la consulta en minutos",
                example = "30")
        Integer consultDurationMinutes
) {}