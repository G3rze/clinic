package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Optional<Medicine> findByBrandName(String brandName);
    Optional<Medicine> findByGenericName(String genericName);
    Optional<Medicine> findByAtcCode(String atcCode);
}