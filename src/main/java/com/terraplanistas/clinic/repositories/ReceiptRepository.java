package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByTransactionId(
            String transactionId
    );
}