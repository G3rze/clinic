package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record RoleResponse(
    UUID id,
    String code,
    String name
) {}