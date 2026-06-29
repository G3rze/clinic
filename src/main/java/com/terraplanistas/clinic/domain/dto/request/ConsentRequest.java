package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para registrar la aceptación de los consentimientos informados " +
        "requeridos durante el proceso de registro del paciente")
public record ConsentRequest(
        @NotNull(message = "Treatment purpose consent is required")
        @Schema(description = "Consentimiento para el uso de datos con fines de tratamiento médico. " +
                "El paciente autoriza que su información sea utilizada para proporcionar " +
                "servicios de salud, diagnósticos y tratamientos",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean treatmentPurpose,

        @NotNull(message = "Data analysis purpose consent is required")
        @Schema(description = "Consentimiento para el uso de datos con fines de análisis estadístico " +
                "y de investigación clínica. Los datos serán anonimizados para proteger " +
                "la identidad del paciente",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean dataAnalysisPurpose
) {}