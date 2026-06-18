package com.terraplanistas.clinic.domain.dto.response;

public record AppointmentTransactionResponse(
    AppointmentResponse appointment
    // TODO: Add payment result fields after PaymentInfo is implemented
    // - paymentId: String
    // - paymentStatus: String
    // - paymentAmount: BigDecimal
    // - receiptUrl: String
) {}
