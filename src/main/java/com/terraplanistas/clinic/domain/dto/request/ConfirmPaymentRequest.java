package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
    @NotBlank(message = "Payment intent ID is required")
    String paymentIntentId
) {}
