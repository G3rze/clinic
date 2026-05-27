package com.terraplanistas.clinic.domain.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserConsentResponse(
    UUID userId,
    String version,
    UUID patientId,
    Boolean treatmentPurpose,
    Boolean dataAnalysisPurpose,
    OffsetDateTime consentedAt
) {}