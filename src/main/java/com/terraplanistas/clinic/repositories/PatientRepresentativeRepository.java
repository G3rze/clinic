package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.PatientRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepresentativeRepository extends JpaRepository<PatientRepresentative, UUID> {
    List<PatientRepresentative> findByPatientId(UUID patientId);
    List<PatientRepresentative> findByRepresentativeUserId(UUID representativeUserId);
    List<PatientRepresentative> findByPatientIdAndDeletedAtIsNull(UUID patientId);
    List<PatientRepresentative> findByRepresentativeUserIdAndDeletedAtIsNull(UUID userId);
}