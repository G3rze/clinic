package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.ReceiptRequest;
import com.terraplanistas.clinic.domain.dto.response.ReceiptResponse;
import com.terraplanistas.clinic.domain.entities.Receipt;

import java.util.UUID;

public class ReceiptMapper {

    public static Receipt toEntity(ReceiptRequest request) {
        Receipt receipt = new Receipt();
        receipt.setAmount(request.amount());
        receipt.setPaymentStatus(request.paymentStatus());
        receipt.setTransactionId(request.transactionId());
        receipt.setBillingDetails(request.billingDetails());
        return receipt;
    }

    public static Receipt toUpgrade(ReceiptRequest request, Receipt receipt) {
        receipt.setAmount(request.amount());
        receipt.setPaymentStatus(request.paymentStatus());
        receipt.setTransactionId(request.transactionId());
        receipt.setBillingDetails(request.billingDetails());
        return receipt;
    }

    public static ReceiptResponse toResponse(Receipt receipt, UUID appointmentId) {
        return new ReceiptResponse(
            receipt.getId(),
            appointmentId,
            receipt.getAmount(),
            receipt.getPaymentStatus(),
            receipt.getTransactionId(),
            receipt.getBillingDetails(),
            receipt.getCreatedAt(),
            receipt.getUpdatedAt()
        );
    }
}