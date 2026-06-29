package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO con la información completa de un medicamento registrado en el catálogo farmacéutico")
public record MedicineResponse(
        @Schema(description = "UUID único del medicamento en el sistema",
                example = "bb0e8400-e29b-41d4-a716-446655440011")
        UUID id,

        @Schema(description = "UUID del laboratorio farmacéutico fabricante",
                example = "aa0e8400-e29b-41d4-a716-446655440010")
        UUID laboratoryId,

        @Schema(description = "Nombre del laboratorio farmacéutico fabricante",
                example = "Tecnoquímicas S.A.")
        String laboratoryName,

        @Schema(description = "Nombre comercial o de marca del medicamento",
                example = "Acetaminofén MK")
        String brandName,

        @Schema(description = "Nombre genérico o Denominación Común Internacional (DCI) del principio activo",
                example = "Paracetamol",
                nullable = true)
        String genericName,

        @Schema(description = "Composición detallada del medicamento con componentes y concentraciones",
                example = "{\"Paracetamol\": \"500mg\", \"Lactosa monohidrato\": \"100mg\"}")
        Map<String, Object> composition,

        @Schema(description = "Grupo etario o indicación de uso del medicamento",
                example = "ADULTS")
        String useFrom,

        @Schema(description = "Código de clasificación ATC internacional",
                example = "N02BE01")
        String atcCode
) {}