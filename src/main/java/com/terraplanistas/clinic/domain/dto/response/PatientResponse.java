package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.enums.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO con la información básica de un paciente registrado en el sistema")
public record PatientResponse(
        @Schema(description = "UUID único del paciente",
                example = "770e8400-e29b-41d4-a716-446655440002")
        UUID id,

        @Schema(description = "Nombres del paciente",
                example = "María Fernanda")
        String firstName,

        @Schema(description = "Apellidos del paciente",
                example = "González López")
        String lastName,

        @Schema(description = "Número de documento de identidad",
                example = "123456789")
        String idNumber,

        @Schema(description = "Tipo de documento de identidad",
                example = "CC")
        IdType idType,

        @Schema(description = "Dirección de residencia del paciente",
                example = "Calle 123 #45-67, Bogotá")
        String address,

        @Schema(description = "Números de teléfono de contacto",
                example = "+573001234567")
        String phones,

        @Schema(description = "Indica si el paciente está activo en el sistema",
                example = "true")
        Boolean isActive,

        @Schema(description = "Fecha de nacimiento del paciente",
                example = "1990-05-20")
        LocalDate birthdate,

        @Schema(description = "UUID del usuario asociado al paciente en el sistema de autenticación. " +
                "Nulo si el paciente no tiene credenciales de acceso propias",
                example = "550e8400-e29b-41d4-a716-446655440000",
                nullable = true)
        UUID userId
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getIdNumber(),
                patient.getIdType(),
                patient.getAddress(),
                patient.getPhones(),
                patient.getIsActive(),
                patient.getBirthdate(),
                patient.getUser() != null ? patient.getUser().getId() : null
        );
    }
}