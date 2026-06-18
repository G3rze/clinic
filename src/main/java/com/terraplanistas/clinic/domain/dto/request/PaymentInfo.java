package com.terraplanistas.clinic.domain.dto.request;

public record PaymentInfo(
    // TODO: Add Stripe payment fields after payment logic is implemented
    // - amount: BigDecimal (payment amount)
    // - currency: String (e.g., "usd", "eur")
    // - paymentMethodType: String (e.g., "card", "bank_transfer")
    // - customerId: String (Stripe customer ID for saved payment methods)
    // - paymentMethodId: String (specific payment method to charge)
    // - description: String (description shown on Stripe dashboard)
    // - metadata: Map<String, String> (additional data: appointmentId, patientId, etc.)
    // - receiptEmail: String (email to send receipt)
    // - saveCard: Boolean (whether to save the card for future payments)
    // - expand: List<String> (fields to expand in Stripe response)
    String placeholder
) {}
