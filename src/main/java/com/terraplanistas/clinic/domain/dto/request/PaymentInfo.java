package com.terraplanistas.clinic.domain.dto.request;

import java.math.BigDecimal;

public record PaymentInfo(
        String currency,
        String description
) {}
