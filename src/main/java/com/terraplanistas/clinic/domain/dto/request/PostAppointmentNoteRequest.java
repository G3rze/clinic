package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PostAppointmentNoteRequest(
    @NotNull(message = "Appointment ID is required")
    UUID appointmentId,

    @NotBlank(message = "Content is required")
    String content
) {}