package com.terraplanistas.clinic.http.stripe.dto;

import java.math.BigDecimal;

public record RefundResponse(
    String id,
    String status,
    BigDecimal amount,
    String paymentIntentId
) {}
