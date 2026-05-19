package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByUserId(UUID userId);
    List<Patient> findByIsActiveTrue();
    Optional<Patient> findByUserIdAndIsActiveTrue(UUID userId);
    Optional<Patient> findByFirstNameBindex(String firstNameBindex);
    Optional<Patient> findByLastNameBindex(String lastNameBindex);
    Optional<Patient> findByIdNumberBindex(String idNumberBindex);
}