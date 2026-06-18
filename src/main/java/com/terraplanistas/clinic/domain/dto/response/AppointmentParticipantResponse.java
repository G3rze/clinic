package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record AppointmentParticipantResponse(
    UUID id,
    String name,
    String email,
    String role,
    String participantType
) {}
