package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record MedicineRequest(
    @NotNull(message = "Laboratory ID is required")
    UUID laboratoryId,

    @NotBlank(message = "Brand name is required")
    String brandName,

    String genericName,

    @NotNull(message = "Composition is required")
    Map<String, Object> composition,

    @NotBlank(message = "Use from is required")
    String useFrom,

    @NotBlank(message = "ATC code is required")
    String atcCode
) {}