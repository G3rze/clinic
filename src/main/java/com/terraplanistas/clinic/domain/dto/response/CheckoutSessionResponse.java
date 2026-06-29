package com.terraplanistas.clinic.domain.dto.response;

import java.util.UUID;

public record CheckoutSessionResponse(
    String checkoutUrl,
    String sessionId,
    UUID appointmentId
) {}
