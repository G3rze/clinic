package com.terraplanistas.clinic.domain.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientRepresentativeResponse(
    UUID id,
    UUID patientId,
    UUID representativeUserId,
    String relationshipType,
    OffsetDateTime deletedAt
) {}