package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import com.terraplanistas.clinic.domain.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record AddDependentRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "\\d{9}", message = "ID number must be exactly 9 digits")
    String idNumber,

    @NotNull(message = "ID type is required")
    IdType idType,

    @NotNull(message = "Birthdate is required")
    @Past(message = "Birthdate must be in the past")
    LocalDate birthdate,

    String address,

    String phones,

    @NotNull(message = "Relationship type is required")
    RelationshipType relationshipType,

    UUID representativeId
) {}