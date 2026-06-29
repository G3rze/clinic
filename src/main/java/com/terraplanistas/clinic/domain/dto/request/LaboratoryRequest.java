package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para registrar o actualizar un laboratorio farmacéutico en el sistema")
public record LaboratoryRequest(
        @Schema(description = "Nombre del laboratorio farmacéutico. Debe ser único en el sistema",
                example = "Tecnoquímicas S.A.")
        String name
) {}