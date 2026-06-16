package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByUserId(UUID userId);
    List<Patient> findByIsActiveTrue();
    Optional<Patient> findByUserIdAndIsActiveTrue(UUID userId);
    Optional<Patient> findByFirstNameBindex(String firstNameBindex);
    Optional<Patient> findByLastNameBindex(String lastNameBindex);
    Optional<Patient> findByIdNumberBindex(String idNumberBindex);
}