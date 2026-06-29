package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.Patient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO con la información completa del perfil del paciente, " +
        "incluyendo indicadores de estado del proceso de registro")
public record PatientProfileResponse(
        @Schema(description = "UUID único del paciente en el sistema",
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

        @Schema(description = "Tipo de documento de identidad (CC, TI, CE, PASSPORT)",
                example = "CC")
        String idType,

        @Schema(description = "Dirección de residencia registrada",
                example = "Carrera 15 #98-45, Apto 302, Bogotá D.C.")
        String address,

        @Schema(description = "Números de teléfono de contacto",
                example = "+573001234567")
        String phones,

        @Schema(description = "Fecha de nacimiento del paciente",
                example = "1990-05-20")
        LocalDate birthdate,

        @Schema(description = "Indica si el perfil del paciente está completamente diligenciado " +
                "con todos los datos requeridos",
                example = "true")
        boolean isComplete,

        @Schema(description = "Indica si el paciente es mayor de edad (18 años o más)",
                example = "true")
        boolean isAdult,

        @Schema(description = "Indica si el paciente ha otorgado los consentimientos informados requeridos",
                example = "true")
        boolean hasConsent
) {
    public static PatientProfileResponse from(Patient patient, boolean isComplete, boolean isAdult, boolean hasConsent) {
        return new PatientProfileResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getIdNumber(),
                patient.getIdType().name(),
                patient.getAddress(),
                patient.getPhones(),
                patient.getBirthdate(),
                isComplete,
                isAdult,
                hasConsent
        );
    }
}