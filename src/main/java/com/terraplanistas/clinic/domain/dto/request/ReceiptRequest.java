package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ReceiptRequest(
    @NotNull(message = "Appointment ID is required")
    UUID appointmentId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,

    @NotNull(message = "Payment status is required")
    PaymentStatus paymentStatus,

    @NotBlank(message = "Transaction ID is required")
    String transactionId,

    String billingDetails
) {}