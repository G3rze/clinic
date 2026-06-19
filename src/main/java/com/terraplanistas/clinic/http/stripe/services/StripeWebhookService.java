package com.terraplanistas.clinic.http.stripe.services;

import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Receipt;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.enums.PaymentStatus;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final ReceiptRepository receiptRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void handlePaymentSucceeded(String paymentIntentId) {

        Receipt receipt = receiptRepository
                .findByTransactionId(paymentIntentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receipt not found for payment intent: "
                                        + paymentIntentId
                        )
                );

        receipt.setPaymentStatus(PaymentStatus.PAID);
        if (receipt.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }


        receiptRepository.save(receipt);

        Appointment appointment = appointmentRepository
                .findByReceiptId(receipt.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found for receipt: "
                                        + receipt.getId()
                        )
                );

        appointment.setStatus(AppointmentStatus.SCHEDULED);

        appointmentRepository.save(appointment);
    }

    @Transactional
    public void handlePaymentFailed(String paymentIntentId) {

        Receipt receipt = receiptRepository
                .findByTransactionId(paymentIntentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receipt not found for payment intent: "
                                        + paymentIntentId
                        )
                );

        receipt.setPaymentStatus(PaymentStatus.FAILED);

        receiptRepository.save(receipt);
    }
}