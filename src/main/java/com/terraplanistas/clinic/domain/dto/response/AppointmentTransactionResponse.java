package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO de respuesta que contiene la información completa de la transacción de agendamiento y pago de una cita médica")
public record AppointmentTransactionResponse(
        @Schema(description = "Información detallada de la cita médica creada o actualizada durante la transacción")
        AppointmentResponse appointment,

        @Schema(description = "Identificador del PaymentIntent de Stripe generado para esta transacción. " +
                "Utilizado para confirmar, cancelar o rastrear el estado del pago",
                example = "pi_3NqL4sBxYV7KpW2q1mZfRtGy")
        String paymentIntentId,

        @Schema(description = "Estado actual del proceso de pago en Stripe. Valores posibles: " +
                "'requires_payment_method' (requiere método de pago), " +
                "'requires_confirmation' (requiere confirmación), " +
                "'processing' (procesando), " +
                "'succeeded' (exitoso), " +
                "'canceled' (cancelado)",
                example = "requires_payment_method")
        String paymentStatus,

        @Schema(description = "Cliente secreto (client_secret) del PaymentIntent. " +
                "Necesario para completar el pago en el frontend cuando se utiliza Stripe Elements " +
                "o para confirmar el pago desde el cliente",
                example = "pi_3NqL4sBxYV7KpW2q1mZfRtGy_secret_aBcDeFgHiJkLmNoPqRsTuVwXyZ")
        String clientSecret,

        @Schema(description = "Monto total del pago procesado en la moneda configurada (ej. USD, COP). " +
                "Incluye el valor de la consulta más impuestos y cargos adicionales aplicables",
                example = "150.00")
        BigDecimal paymentAmount
) {}