package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.UserConsent;
import com.terraplanistas.clinic.domain.entities.UserConsentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, UserConsentId> {
    List<UserConsent> findByUserId(UUID userId);
    List<UserConsent> findByPatientId(UUID patientId);
    List<UserConsent> findByUserIdAndPatientId(UUID userId, UUID patientId);
    Optional<UserConsent> findByUserIdAndPatientIdAndVersion(UUID userId, UUID patientId, String version);
}