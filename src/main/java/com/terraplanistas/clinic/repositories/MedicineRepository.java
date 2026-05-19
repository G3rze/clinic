package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Optional<Medicine> findByBrandName(String brandName);
    Optional<Medicine> findByGenericName(String genericName);
    Optional<Medicine> findByAtcCode(String atcCode);
}