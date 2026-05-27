package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LaboratoryRequest(
    @NotBlank(message = "Name is required")
    String name
) {}