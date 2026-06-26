package com.terraplanistas.clinic.domain.dto.request;

public record PaymentInfo(
        String currency,
        String description
) {}
