package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record InitRegistrationResponse(
    UUID pendingUserConfigId,
    boolean isAdult,
    String message
) {}
