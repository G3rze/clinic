package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para confirmar el pago de una cita médica mediante el ID del PaymentIntent de Stripe")
public record ConfirmPaymentRequest(
        @NotBlank(message = "Payment intent ID is required")
        @Schema(description = "Identificador del PaymentIntent de Stripe generado durante la creación de la transacción de pago. " +
                "Este ID es único y se utiliza para confirmar que el pago fue procesado exitosamente " +
                "en la pasarela de pagos. Formato típico: pi_XXXXXXXXXXXXXX",
                example = "pi_3NqL4sBxYV7KpW2q1mZfRtGy",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String paymentIntentId
) {}