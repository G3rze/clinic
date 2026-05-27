package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserConsentRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotBlank(message = "Version is required")
    String version,

    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Treatment purpose consent is required")
    Boolean treatmentPurpose,

    @NotNull(message = "Data analysis purpose consent is required")
    Boolean dataAnalysisPurpose
) {}