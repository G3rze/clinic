package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PatientProfileRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "\\d{9}", message = "DUI must be exactly 9 digits")
    String idNumber,

    @NotNull(message = "ID type is required")
    IdType idType,

    @NotBlank(message = "Address is required")
    String address,

    String phones,

    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    LocalDate birthdate
) {}