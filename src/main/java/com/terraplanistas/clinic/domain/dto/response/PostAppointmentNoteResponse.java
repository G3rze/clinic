package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO con la información de una nota post-consulta registrada por el médico tratante")
public record PostAppointmentNoteResponse(
        @Schema(description = "UUID único de la nota post-consulta generado por el sistema",
                example = "dd0e8400-e29b-41d4-a716-446655440013")
        UUID id,

        @Schema(description = "UUID de la cita médica a la que pertenece esta nota post-consulta",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID appointmentId,

        @Schema(description = "Contenido completo de la nota post-consulta con las observaciones y " +
                "recomendaciones registradas por el médico",
                example = "Se recomienda realizar control de presión arterial diario durante 7 días " +
                        "y registrar los resultados para revisión en próxima consulta...")
        String content
) {}