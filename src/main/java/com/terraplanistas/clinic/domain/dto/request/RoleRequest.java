package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
    @NotBlank(message = "Code is required")
    String code,

    @NotBlank(message = "Name is required")
    String name
) {}