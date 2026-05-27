package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReceiptResponse(
    UUID id,
    UUID appointmentId,
    BigDecimal amount,
    PaymentStatus paymentStatus,
    String transactionId,
    String billingDetails,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}