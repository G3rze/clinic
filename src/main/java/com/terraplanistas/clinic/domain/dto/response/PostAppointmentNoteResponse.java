package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record PostAppointmentNoteResponse(
    UUID id,
    UUID appointmentId,
    String content
) {}