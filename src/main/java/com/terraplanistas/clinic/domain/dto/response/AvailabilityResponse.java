package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.util.UUID;

@Schema(description = "DTO que representa una franja de disponibilidad horaria recurrente de un médico")
public record AvailabilityResponse(
        @Schema(description = "UUID único de la franja de disponibilidad",
                example = "990e8400-e29b-41d4-a716-446655440004")
        UUID id,

        @Schema(description = "UUID del médico al que pertenece esta disponibilidad",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID employeeId,

        @Schema(description = "UUID de la especialidad médica asociada a esta disponibilidad",
                example = "880e8400-e29b-41d4-a716-446655440003")
        UUID specialtyId,

        @Schema(description = "Día de la semana programado para esta disponibilidad",
                example = "MONDAY")
        DayOfWeek dayOfWeek,

        @Schema(description = "Hora de inicio de la franja horaria con offset horario",
                example = "08:00:00-05:00")
        OffsetTime startTime,

        @Schema(description = "Hora de finalización de la franja horaria con offset horario",
                example = "17:00:00-05:00")
        OffsetTime endTime,

        @Schema(description = "Indica si la franja de disponibilidad está activa y se considera para agendamiento",
                example = "true")
        Boolean isActive
) {}