package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "DTO para registrar una nota post-consulta asociada a una cita médica finalizada. " +
        "Estas notas complementan la historia clínica con observaciones adicionales del médico " +
        "posteriores a la atención")
public record PostAppointmentNoteRequest(
        @NotNull(message = "Appointment ID is required")
        @Schema(description = "UUID de la cita médica a la que se asocia esta nota post-consulta. " +
                "La cita debe estar en estado COMPLETED para poder agregar notas",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID appointmentId,

        @NotBlank(message = "Content is required")
        @Schema(description = "Contenido de la nota post-consulta. Texto libre donde el médico puede registrar: " +
                "observaciones adicionales, recomendaciones de seguimiento, indicaciones especiales " +
                "para el paciente, cambios en el tratamiento o cualquier información relevante " +
                "que no quedó registrada en la historia clínica principal",
                example = "Se recomienda realizar control de presión arterial diario durante 7 días " +
                        "y registrar los resultados para revisión en próxima consulta. " +
                        "Iniciar dieta baja en sodio y reducir consumo de cafeína. " +
                        "Programar cita de control en 2 semanas.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {}