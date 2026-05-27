package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record LaboratoryResponse(
    UUID id,
    String name
) {}