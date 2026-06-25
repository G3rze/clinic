package com.terraplanistas.clinic.http.stripe.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;


@Builder
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
