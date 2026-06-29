package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO con la información completa de una receta médica digital, " +
        "incluyendo estado de dispensaciones y datos de trazabilidad")
public record PrescriptionResponse(
        @Schema(description = "UUID único de la receta médica generado por el sistema",
                example = "ee0e8400-e29b-41d4-a716-446655440014")
        UUID id,

        @Schema(description = "UUID de la cita médica en la que se emitió la receta",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID appointmentId,

        @Schema(description = "UUID del medicamento prescrito en el catálogo farmacéutico",
                example = "bb0e8400-e29b-41d4-a716-446655440011")
        UUID medicineId,

        @Schema(description = "Instantánea de los datos del medicamento al momento de la prescripción. " +
                "Incluye nombre, composición y clasificación ATC",
                example = "{\"brandName\": \"Ibuprofeno MK\", \"genericName\": \"Ibuprofeno\", " +
                        "\"atcCode\": \"M01AE01\"}")
        Map<String, Object> medicineSnapshot,

        @Schema(description = "Instrucciones detalladas de dosificación y administración del medicamento",
                example = "Tomar 1 tableta de 400mg cada 8 horas por vía oral durante 5 días")
        String dosageInstructions,

        @Schema(description = "Firma digital del médico que garantiza la autenticidad de la prescripción",
                example = "MEUCIh0A9vR7K3mN8xL2pQ5wYbF4jT6sV1dG...")
        String digitalSignature,

        @Schema(description = "Número de dispensaciones realizadas hasta el momento. " +
                "Se incrementa automáticamente en cada dispensación",
                example = "1")
        Integer usageCount,

        @Schema(description = "Número máximo de dispensaciones permitidas para esta receta (1-3)",
                example = "2")
        Integer maxUsages,

        @Schema(description = "Fecha y hora de creación de la receta en formato ISO-8601",
                example = "2026-07-03T16:45:00-05:00")
        OffsetDateTime createdAt,

        @Schema(description = "UUID del médico que creó la receta. Referencia al empleado del sistema",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID createdBy
) {}