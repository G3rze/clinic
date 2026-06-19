package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentCancellationResponse(
        UUID appointmentId,
        String status,
        BigDecimal refundedAmount
) {
}