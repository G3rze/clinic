package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentCreationRequest(
        @NotNull(message = "Debe incluir un id de empleado")
        UUID employeeId,
        @NotNull(message = "Debe incluir un id de paciente")
        UUID patientId,
        @NotNull(message = "Debe incluir una fecha de incio")
        OffsetDateTime startAt
) {}
