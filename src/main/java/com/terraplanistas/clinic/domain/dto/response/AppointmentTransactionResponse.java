package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;

public record AppointmentTransactionResponse(
        AppointmentResponse appointment,
        String paymentIntentId,
        String paymentStatus,
        String clientSecret,
        BigDecimal paymentAmount
) {
}