package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO con la información de un laboratorio farmacéutico registrado en el sistema")
public record LaboratoryResponse(
        @Schema(description = "UUID único del laboratorio",
                example = "aa0e8400-e29b-41d4-a716-446655440010")
        UUID id,

        @Schema(description = "Nombre del laboratorio farmacéutico",
                example = "Tecnoquímicas S.A.")
        String name
) {}