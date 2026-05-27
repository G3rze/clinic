package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.ReceiptRequest;
import com.terraplanistas.clinic.domain.dto.response.ReceiptResponse;
import com.terraplanistas.clinic.domain.entities.Receipt;
import com.terraplanistas.clinic.domain.entities.Appointment;

public class ReceiptMapper {

    public static Receipt toEntity(ReceiptRequest request, Appointment appointment) {
        Receipt receipt = new Receipt();
        receipt.setAppointment(appointment);
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

    public static ReceiptResponse toResponse(Receipt receipt) {
        return new ReceiptResponse(
            receipt.getId(),
            receipt.getAppointment() != null ? receipt.getAppointment().getId() : null,
            receipt.getAmount(),
            receipt.getPaymentStatus(),
            receipt.getTransactionId(),
            receipt.getBillingDetails(),
            receipt.getCreatedAt(),
            receipt.getUpdatedAt()
        );
    }
}