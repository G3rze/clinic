package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO de respuesta con la información de la sesión de checkout de Stripe creada para el pago de una cita")
public record CheckoutSessionResponse(
        @Schema(description = "URL de redirección al portal de pago seguro de Stripe donde el usuario " +
                "deberá completar la transacción ingresando sus datos de pago",
                example = "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4e5f6g7h8i9j0")
        String checkoutUrl,

        @Schema(description = "Identificador único de la sesión de checkout en Stripe. " +
                "Utilizado para verificar el estado de la sesión y confirmar la transacción",
                example = "cs_test_a1b2c3d4e5f6g7h8i9j0")
        String sessionId,

        @Schema(description = "UUID de la cita médica asociada a esta sesión de pago",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID appointmentId
) {}