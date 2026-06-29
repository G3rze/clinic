package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO con la información pública de un médico disponible para pacientes. " +
        "Incluye datos básicos de identificación y las especialidades que ejerce")
public record PublicDoctorResponse(
        @Schema(description = "UUID del médico en el sistema",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID id,

        @Schema(description = "Nombre del médico",
                example = "Carlos Alberto")
        String firstName,

        @Schema(description = "Apellido del médico",
                example = "Rodríguez Mendoza")
        String lastName,

        @Schema(description = "Código del rol del médico en el sistema",
                example = "DOCTOR")
        String roleCode,

        @Schema(description = "Lista de especialidades médicas que ejerce el médico con información resumida")
        List<PublicSpecialtyResponse> specialties
) {}