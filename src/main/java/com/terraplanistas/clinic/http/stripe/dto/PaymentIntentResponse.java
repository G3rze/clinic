package com.terraplanistas.clinic.http.stripe.dto;

import java.math.BigDecimal;

public record PaymentIntentResponse(
    String id,
    String status,
    String clientSecret,
    BigDecimal amount,
    String currency,
    String receiptId
) {}
