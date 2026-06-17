package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
    @NotNull(message = "Treatment purpose consent is required")
    Boolean treatmentPurpose,

    @NotNull(message = "Data analysis purpose consent is required")
    Boolean dataAnalysisPurpose
) {}