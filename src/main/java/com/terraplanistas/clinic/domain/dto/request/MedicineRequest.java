package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO para registrar o actualizar un medicamento en el catálogo farmacéutico del sistema. " +
        "Incluye información del laboratorio fabricante, nombres comerciales y genéricos, " +
        "composición química y clasificación ATC")
public record MedicineRequest(
        @NotNull(message = "Laboratory ID is required")
        @Schema(description = "UUID del laboratorio farmacéutico fabricante del medicamento",
                example = "aa0e8400-e29b-41d4-a716-446655440010",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID laboratoryId,

        @NotBlank(message = "Brand name is required")
        @Schema(description = "Nombre comercial o de marca del medicamento. Es el nombre bajo el cual " +
                "se comercializa el producto farmacéutico",
                example = "Acetaminofén MK",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String brandName,

        @Schema(description = "Nombre genérico o Denominación Común Internacional (DCI) del principio activo. " +
                "Opcional si el medicamento es conocido principalmente por su nombre comercial",
                example = "Paracetamol",
                nullable = true)
        String genericName,

        @NotNull(message = "Composition is required")
        @Schema(description = "Composición detallada del medicamento. Mapa clave-valor donde se especifican " +
                "los componentes y sus concentraciones. Ejemplo: {\"Paracetamol\": \"500mg\", " +
                "\"Excipientes\": \"c.s.\"}",
                example = "{\"Paracetamol\": \"500mg\", \"Lactosa monohidrato\": \"100mg\"}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, Object> composition,

        @NotBlank(message = "Use from is required")
        @Schema(description = "Indicación de uso o grupo etario para el cual está formulado el medicamento. " +
                "Valores típicos: 'ADULTS', 'CHILDREN', 'INFANTS', 'ELDERLY', 'ALL_AGES'",
                example = "ADULTS",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String useFrom,

        @NotBlank(message = "ATC code is required")
        @Schema(description = "Código ATC (Anatomical Therapeutic Chemical Classification System) de la OMS. " +
                "Sistema de clasificación internacional que codifica el medicamento según su " +
                "acción terapéutica y estructura química. Formato: letra + 6 caracteres alfanuméricos",
                example = "N02BE01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String atcCode
) {}