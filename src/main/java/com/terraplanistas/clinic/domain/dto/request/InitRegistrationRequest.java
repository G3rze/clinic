package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record InitRegistrationRequest(
    @NotBlank(message = "Google user ID is required")
    String googleUserId,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    String name,

    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    LocalDate birthdate
) {}
