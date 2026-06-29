package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "DTO para registrar la calificación y reseña de una cita médica completada. " +
        "Permite evaluar la calidad del servicio médico recibido en una escala de 1 a 5 estrellas")
public record RateAppointmentRequest(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "Identificador único universal (UUID) del paciente que realizó la consulta y " +
                "que está calificando la atención recibida. Debe coincidir con el paciente " +
                "registrado como titular de la cita",
                example = "770e8400-e29b-41d4-a716-446655440002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID patientId,

        @NotNull(message = "Score is required")
        @Min(value = 1, message = "Score must be at least 1")
        @Max(value = 5, message = "Score must be at most 5")
        @Schema(description = "Puntuación otorgada a la atención médica recibida en una escala del 1 al 5, donde: " +
                "1 = Muy insatisfactoria - Experiencia extremadamente negativa, " +
                "múltiples problemas graves en la atención\n" +
                "2 = Insatisfactoria - Experiencia por debajo de lo esperado, " +
                "problemas significativos en el servicio\n" +
                "3 = Aceptable - Experiencia dentro de lo esperado, " +
                "servicio competente sin aspectos destacables\n" +
                "4 = Satisfactoria - Buena experiencia, " +
                "atención profesional que supera las expectativas básicas\n" +
                "5 = Excelente - Experiencia excepcional, " +
                "atención de máxima calidad que excede ampliamente las expectativas",
                example = "5",
                minimum = "1",
                maximum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer score,

        @Size(max = 1000, message = "Review cannot exceed 1000 characters")
        @Schema(description = "Reseña o comentario detallado del paciente sobre su experiencia en la consulta. " +
                "Campo opcional de texto libre con un máximo de 1000 caracteres. " +
                "Se recomienda incluir aspectos como: trato del profesional, claridad del diagnóstico, " +
                "tiempo de espera, instalaciones, efectividad del tratamiento recomendado, " +
                "y cualquier otra observación relevante que pueda ser útil para otros pacientes " +
                "y para la mejora continua del servicio",
                example = "Excelente atención del Dr. Rodríguez. Me explicó detalladamente mi diagnóstico, " +
                        "resolvió todas mis dudas con paciencia y profesionalismo. El tiempo de espera fue mínimo " +
                        "y las instalaciones muy limpias. Totalmente recomendado.",
                maxLength = 1000,
                nullable = true)
        String review
) {}