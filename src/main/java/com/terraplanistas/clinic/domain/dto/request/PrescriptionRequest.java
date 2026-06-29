package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO para la creación de una receta médica digital. " +
        "Contiene la información del medicamento prescrito, dosis, firma digital del médico " +
        "y control de dispensaciones permitidas")
public record PrescriptionRequest(
        @NotNull(message = "Appointment ID is required")
        @Schema(description = "UUID de la cita médica en la que se emite esta receta. " +
                "Vincula la prescripción con la consulta y la historia clínica correspondiente",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID appointmentId,

        @NotNull(message = "Medicine ID is required")
        @Schema(description = "UUID del medicamento prescrito en el catálogo farmacéutico del sistema",
                example = "bb0e8400-e29b-41d4-a716-446655440011",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID medicineId,

        @NotNull(message = "Medicine snapshot is required")
        @Schema(description = "Instantánea de los datos del medicamento en el momento de la prescripción. " +
                "Incluye nombre comercial, genérico, composición y código ATC. " +
                "Garantiza trazabilidad incluso si el catálogo se modifica posteriormente",
                example = "{\"brandName\": \"Ibuprofeno MK\", \"genericName\": \"Ibuprofeno\", " +
                        "\"composition\": {\"Ibuprofeno\": \"400mg\"}, \"atcCode\": \"M01AE01\"}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, Object> medicineSnapshot,

        @NotBlank(message = "Dosage instructions are required")
        @Schema(description = "Instrucciones detalladas de dosificación para el paciente. " +
                "Debe incluir: dosis, frecuencia, vía de administración, duración del tratamiento " +
                "y consideraciones especiales (tomar con alimentos, evitar alcohol, etc.)",
                example = "Tomar 1 tableta de 400mg cada 8 horas por vía oral durante 5 días. " +
                        "No exceder 3 tabletas en 24 horas. Tomar con alimentos para evitar irritación gástrica.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String dosageInstructions,

        @NotBlank(message = "Digital signature is required")
        @Schema(description = "Firma digital del médico que emite la receta. " +
                "Garantiza la autenticidad e integridad de la prescripción médica. " +
                "Se genera mediante el sistema de firma electrónica de la institución",
                example = "MEUCIh0A9vR7K3mN8xL2pQ5wYbF4jT6sV1dG...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String digitalSignature,

        @Min(value = 0, message = "Usage count cannot be negative")
        @Schema(description = "Contador de dispensaciones realizadas. Inicia en 0 y se incrementa " +
                "cada vez que la farmacia dispensa el medicamento. No debe ser negativo",
                example = "0",
                defaultValue = "0",
                minimum = "0")
        Integer usageCount,

        @Min(value = 1, message = "Max usages must be at least 1")
        @Max(value = 3, message = "Max usages cannot exceed 3")
        @Schema(description = "Número máximo de dispensaciones permitidas para esta receta. " +
                "Valores permitidos: 1, 2 o 3. Controla la cantidad de veces que " +
                "el paciente puede reclamar el medicamento en farmacia",
                example = "2",
                minimum = "1",
                maximum = "3")
        Integer maxUsages
) {}