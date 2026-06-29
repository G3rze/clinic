package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "DTO para actualizar la información del perfil de un paciente existente. " +
        "Permite modificar datos personales, documento de identidad, dirección y contacto")
public record PatientProfileRequest(
        @NotBlank(message = "First name is required")
        @Schema(description = "Nombres completos del paciente",
                example = "María Fernanda",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Apellidos completos del paciente",
                example = "González López",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @NotBlank(message = "ID number is required")
        @Pattern(regexp = "\\d{9}", message = "DUI must be exactly 9 digits")
        @Schema(description = "Número de documento de identidad. Exactamente 9 dígitos numéricos",
                example = "123456789",
                pattern = "\\d{9}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idNumber,

        @NotNull(message = "ID type is required")
        @Schema(description = "Tipo de documento de identidad: CC, TI, CE, PASSPORT",
                example = "CC",
                requiredMode = Schema.RequiredMode.REQUIRED)
        IdType idType,

        @NotBlank(message = "Address is required")
        @Schema(description = "Dirección de residencia completa del paciente",
                example = "Carrera 15 #98-45, Apto 302, Bogotá D.C.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String address,

        @Schema(description = "Números de teléfono de contacto. Formato: +[código país][número]",
                example = "+573001234567",
                nullable = true)
        String phones,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        @Schema(description = "Fecha de nacimiento del paciente en formato ISO (YYYY-MM-DD). Debe ser una fecha pasada",
                example = "1990-05-20",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate birthdate
) {}