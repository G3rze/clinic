package com.terraplanistas.clinic.http.stripe.dto;

import java.math.BigDecimal;

public record RefundRequest(
    String paymentIntentId,
    BigDecimal amount
) {}
