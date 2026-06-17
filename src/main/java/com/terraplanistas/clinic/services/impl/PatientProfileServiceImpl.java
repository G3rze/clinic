package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.ConsentRequest;
import com.terraplanistas.clinic.domain.dto.request.PatientProfileRequest;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.UserConsent;
import com.terraplanistas.clinic.http.security.SecurityProperties;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.UserConsentRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.PatientProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PatientProfileServiceImpl implements PatientProfileService {

    private static final int ADULT_AGE = 18;
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final PatientRepository patientRepository;
    private final UserConsentRepository userConsentRepository;
    private final SecurityProperties securityProperties;

    public PatientProfileServiceImpl(PatientRepository patientRepository,
                                       UserRepository userRepository,
                                       UserConsentRepository userConsentRepository,
                                       SecurityProperties securityProperties) {
        this.patientRepository = patientRepository;
        this.userConsentRepository = userConsentRepository;
        this.securityProperties = securityProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found for user: " + userId));
    }

    @Override
    @Transactional
    public Patient updateProfile(UUID userId, PatientProfileRequest request) {
        Patient patient = getPatientByUserId(userId);

        if (!isPatientAdult(request.birthdate())) {
            throw new IllegalArgumentException("Patient must be at least 18 years old");
        }

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setIdNumber(request.idNumber());
        patient.setIdType(request.idType());
        patient.setAddress(request.address());
        patient.setPhones(request.phones() != null ? request.phones() : "");
        patient.setBirthdate(request.birthdate());
        patient.setUpdatedBy(SYSTEM_USER_ID);

        return patientRepository.save(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileComplete(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) {
            return false;
        }
        return patient.getFirstName() != null && !patient.getFirstName().isBlank() &&
               patient.getLastName() != null && !patient.getLastName().isBlank() &&
               patient.getIdNumber() != null && !patient.getIdNumber().isBlank() &&
               patient.getBirthdate() != null &&
               patient.getAddress() != null && !patient.getAddress().isBlank();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isConsentGiven(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) {
            return false;
        }
        String version = securityProperties.getConsent().getVersion();
        return userConsentRepository
                .findByUserIdAndPatientIdAndVersion(userId, patient.getId(), version)
                .isPresent();
    }

    @Override
    @Transactional
    public UserConsent recordConsent(UUID userId, ConsentRequest request) {
        Patient patient = getPatientByUserId(userId);

        if (!isPatientAdult(patient.getBirthdate())) {
            throw new IllegalArgumentException("Patient must be at least 18 years old to give consent");
        }

        String version = securityProperties.getConsent().getVersion();

        UserConsent consent = userConsentRepository
                .findByUserIdAndPatientIdAndVersion(userId, patient.getId(), version)
                .orElse(new UserConsent());

        consent.setUserId(userId);
        consent.setPatientId(patient.getId());
        consent.setVersion(version);
        consent.setTreatmentPurpose(request.treatmentPurpose());
        consent.setDataAnalysisPurpose(request.dataAnalysisPurpose());
        consent.setConsentedAt(OffsetDateTime.now());

        return userConsentRepository.save(consent);
    }

    @Override
    @Transactional(readOnly = true)
    public String getCurrentConsentVersion() {
        return securityProperties.getConsent().getVersion();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientAdult(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null || patient.getBirthdate() == null) {
            return false;
        }
        return isPatientAdult(patient.getBirthdate());
    }

    private boolean isPatientAdult(LocalDate birthdate) {
        return birthdate != null && birthdate.plusYears(ADULT_AGE).isBefore(LocalDate.now());
    }
}
