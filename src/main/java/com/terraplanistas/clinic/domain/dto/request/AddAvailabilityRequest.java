package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.DayOfWeek;

@Schema(description = "DTO para agregar una franja de disponibilidad horaria recurrente para un médico " +
        "en un día específico de la semana")
public record AddAvailabilityRequest(
        @NotNull(message = "Day of week is required")
        @Schema(description = "Día de la semana para la disponibilidad horaria. Valores: " +
                "MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY",
                example = "MONDAY",
                requiredMode = Schema.RequiredMode.REQUIRED)
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Invalid start time format (HH:mm)")
        @Schema(description = "Hora de inicio de la disponibilidad en formato 24 horas (HH:mm). " +
                "Ejemplo: 08:00, 14:30",
                example = "08:00",
                pattern = "^([01]\\d|2[0-3]):([0-5]\\d)$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String startTime,

        @NotNull(message = "End time is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Invalid end time format (HH:mm)")
        @Schema(description = "Hora de finalización de la disponibilidad en formato 24 horas (HH:mm). " +
                "Debe ser posterior a la hora de inicio. Ejemplo: 17:00, 18:30",
                example = "17:00",
                pattern = "^([01]\\d|2[0-3]):([0-5]\\d)$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String endTime
) {}