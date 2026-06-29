package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

@Schema(description = "DTO que representa una franja horaria disponible para agendar una consulta médica. " +
        "Incluye los datos del médico, especialidad, horario y tarifa correspondiente")
public record AvailableSlotResponse(
        @Schema(description = "UUID del médico que ofrece la franja horaria",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID doctorId,

        @Schema(description = "Nombre del médico",
                example = "Carlos")
        String doctorFirstName,

        @Schema(description = "Apellido del médico",
                example = "Rodríguez Mendoza")
        String doctorLastName,

        @Schema(description = "Código de la especialidad médica para esta franja",
                example = "CARDIOLOGY")
        String specialtyCode,

        @Schema(description = "Nombre legible de la especialidad médica en español",
                example = "Cardiología")
        String specialtyName,

        @Schema(description = "Fecha de la franja horaria disponible en formato ISO (YYYY-MM-DD)",
                example = "2026-07-03")
        LocalDate date,

        @Schema(description = "Hora de inicio de la franja horaria en formato ISO-8601 con offset",
                example = "09:00:00-05:00")
        OffsetTime startTime,

        @Schema(description = "Hora de finalización de la franja horaria en formato ISO-8601 con offset",
                example = "09:30:00-05:00")
        OffsetTime endTime,

        @Schema(description = "Tarifa por hora de consulta para esta especialidad en la moneda configurada",
                example = "150.00")
        BigDecimal feePerHour,

        @Schema(description = "Duración de la consulta en minutos para esta franja horaria",
                example = "30")
        Integer consultDurationMinutes
) {}