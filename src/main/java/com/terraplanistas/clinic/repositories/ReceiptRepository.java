package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Receipt;
import com.terraplanistas.clinic.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByTransactionId(String transactionId);
    List<Receipt> findByAppointmentId(UUID appointmentId);
    List<Receipt> findByPaymentStatus(PaymentStatus paymentStatus);
}