package com.terraplanistas.clinic.http.stripe.dto;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentIntentCreateRequest(
    BigDecimal amount,
    String currency,
    Map<String, String> metadata
) {
    public PaymentIntentCreateRequest {
        if (currency == null || currency.isBlank()) {
            currency = "usd";
        }
    }
}
