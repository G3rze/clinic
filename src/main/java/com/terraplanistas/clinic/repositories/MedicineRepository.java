package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Optional<Medicine> findByBrandName(String brandName);
    Optional<Medicine> findByGenericName(String genericName);
    Optional<Medicine> findByAtcCode(String atcCode);

    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.brandName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.atcCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Medicine> searchByAnyField(@Param("search") String search, Pageable pageable);
}