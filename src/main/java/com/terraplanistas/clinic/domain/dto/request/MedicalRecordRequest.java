package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO para registrar una entrada en la historia clínica de un paciente. " +
        "Documenta el diagnóstico, notas clínicas y hallazgos del examen físico " +
        "realizados durante una consulta médica")
public record MedicalRecordRequest(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "UUID del paciente al que corresponde esta historia clínica",
                example = "770e8400-e29b-41d4-a716-446655440002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID patientId,

        @NotNull(message = "Appointment ID is required")
        @Schema(description = "UUID de la cita médica asociada a este registro clínico. " +
                "Vincula la historia clínica con la consulta específica",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID appointmentId,

        @NotNull(message = "Employee ID is required")
        @Schema(description = "UUID del médico (empleado) que realiza el registro clínico",
                example = "660e8400-e29b-41d4-a716-446655440001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID employeeId,

        @NotBlank(message = "Diagnosis code is required")
        @Schema(description = "Código del diagnóstico según la clasificación CIE-10 (Clasificación Internacional " +
                "de Enfermedades, 10.ª edición). Identifica de forma estandarizada la condición médica",
                example = "J45.0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String diagnosisCode,

        @NotBlank(message = "Diagnosis description is required")
        @Schema(description = "Descripción detallada del diagnóstico médico. Debe incluir la interpretación " +
                "clínica de los síntomas y hallazgos encontrados",
                example = "Asma predominantemente alérgica con exacerbación moderada. " +
                        "Paciente presenta sibilancias espiratorias y disnea de esfuerzo.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String diagnosisDescription,

        @NotBlank(message = "Clinical notes are required")
        @Schema(description = "Notas clínicas detalladas de la consulta. Incluye anamnesis, evolución, " +
                "tratamiento indicado, recomendaciones y plan de seguimiento",
                example = "Paciente refiere episodios de tos seca nocturna y dificultad respiratoria " +
                        "durante la última semana. Se prescribe salbutamol inhalador 100mcg/dosis, " +
                        "2 puff cada 8 horas por 7 días. Control en 2 semanas.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String clinicalNotes,

        @Schema(description = "Hallazgos del examen físico realizado durante la consulta. " +
                "Incluye signos vitales, auscultación, palpación y demás exploraciones pertinentes",
                example = "FR: 22 rpm, SpO2: 94%, sibilancias espiratorias bilaterales. " +
                        "Tórax simétrico, buena entrada de aire bilateral.",
                nullable = true)
        String physicalExamination,

        @Schema(description = "Lista de URLs o identificadores de archivos adjuntos a la historia clínica. " +
                "Puede incluir resultados de laboratorio, imágenes diagnósticas, " +
                "electrocardiogramas y otros documentos relevantes",
                example = "[\"https://storage.clinica.com/exams/lab-12345.pdf\", " +
                        "\"https://storage.clinica.com/imaging/rx-torax-67890.dcm\"]",
                nullable = true)
        List<String> attachments
) {}