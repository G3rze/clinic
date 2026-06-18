package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotNull;

public record AppointmentTransactionRequest(
    @NotNull(message = "Appointment info is required")
    CreateAppointmentRequest appointmentInfo,

    @NotNull(message = "Payment info is required")
    PaymentInfo paymentInfo
) {}
