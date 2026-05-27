package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PatientRepresentativeRequest(
    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Representative User ID is required")
    UUID representativeUserId,

    @NotBlank(message = "Relationship type is required")
    String relationshipType
) {}