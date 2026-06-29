package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO con la información completa de un registro de historia clínica. " +
        "Contiene todos los datos documentados durante una consulta médica")
public record MedicalRecordResponse(
        @Schema(description = "UUID único del registro de historia clínica",
                example = "cc0e8400-e29b-41d4-a716-446655440012")
        UUID id,

        @Schema(description = "UUID del paciente titular de la historia clínica",
                example = "770e8400-e29b-41d4-a716-446655440002")
        UUID patientId,

        @Schema(description = "UUID de la cita médica asociada al registro",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID appointmentId,

        @Schema(description = "UUID del médico que realizó el registro",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID employeeId,

        @Schema(description = "Nombre completo del médico que atendió la consulta",
                example = "Dr. Carlos Rodríguez Mendoza")
        String doctorName,

        @Schema(description = "Fecha y hora de creación del registro clínico",
                example = "2026-07-03T15:30:00-05:00")
        OffsetDateTime createdAt,

        @Schema(description = "Código CIE-10 del diagnóstico médico",
                example = "J45.0")
        String diagnosisCode,

        @Schema(description = "Descripción detallada del diagnóstico",
                example = "Asma predominantemente alérgica con exacerbación moderada")
        String diagnosisDescription,

        @Schema(description = "Notas clínicas completas de la consulta, incluyendo tratamiento y recomendaciones",
                example = "Paciente refiere episodios de tos seca nocturna...")
        String clinicalNotes,

        @Schema(description = "Hallazgos del examen físico realizado durante la consulta",
                example = "FR: 22 rpm, SpO2: 94%, sibilancias espiratorias bilaterales",
                nullable = true)
        String physicalExamination,

        @Schema(description = "Lista de archivos adjuntos al registro clínico (exámenes, imágenes, etc.)",
                example = "[\"https://storage.clinica.com/exams/lab-12345.pdf\"]",
                nullable = true)
        List<String> attachments
) {}