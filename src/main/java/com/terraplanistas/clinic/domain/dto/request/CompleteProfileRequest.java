package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CompleteProfileRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotNull(message = "ID type is required")
    IdType idType,

    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "\\d{9}", message = "DUI must be exactly 9 digits")
    String idNumber,

    @NotBlank(message = "Address is required")
    String address,

    String phones
) {}
