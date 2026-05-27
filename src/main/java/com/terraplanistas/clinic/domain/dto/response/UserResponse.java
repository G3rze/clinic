package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String username,
    UUID roleId
) {}