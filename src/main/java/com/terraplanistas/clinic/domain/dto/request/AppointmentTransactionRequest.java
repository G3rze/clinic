package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO que encapsula la información completa para crear una cita médica con su transacción de pago asociada")
public record AppointmentTransactionRequest(
        @NotNull(message = "Appointment info is required")
        @Schema(description = "Información detallada de la cita médica a agendar, incluyendo fecha, hora, " +
                "médico asignado, paciente y tipo de consulta",
                requiredMode = Schema.RequiredMode.REQUIRED)
        CreateAppointmentRequest appointmentInfo,

        @NotNull(message = "Payment info is required")
        @Schema(description = "Información del método de pago y monto a procesar para la cita. " +
                "Incluye datos de la tarjeta o token de pago según la pasarela configurada",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PaymentInfo paymentInfo
) {}