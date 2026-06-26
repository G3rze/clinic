package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.AddDependentRequest;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.PatientRepresentative;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.PatientRepresentativeRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.FamilyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FamilyServiceImpl implements FamilyService {

    private static final int ADULT_AGE = 18;
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientRepresentativeRepository representativeRepository;

    public FamilyServiceImpl(PatientRepository patientRepository,
                             UserRepository userRepository,
                             PatientRepresentativeRepository representativeRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.representativeRepository = representativeRepository;
    }

    @Override
    @Transactional
    public Patient addDependent(UUID representativeUserId, AddDependentRequest request) {
        User representative = userRepository.findById(representativeUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + representativeUserId));

        if (!isAdult(request.birthdate())) {
            throw new IllegalArgumentException("Dependent must be a minor (under 18)");
        }

        Patient dependent = new Patient();
        dependent.setFirstName(request.firstName());
        dependent.setLastName(request.lastName());
        dependent.setIdNumber(request.idNumber());
        dependent.setIdType(request.idType());
        dependent.setAddress(request.address() != null ? request.address() : "");
        dependent.setPhones(request.phones() != null ? request.phones() : "");
        dependent.setBirthdate(request.birthdate());
        dependent.setIsActive(true);
        dependent.setCreatedBy(SYSTEM_USER_ID);
        dependent.setUpdatedBy(SYSTEM_USER_ID);

        dependent = patientRepository.save(dependent);

        PatientRepresentative link = new PatientRepresentative();
        link.setPatient(dependent);
        link.setRepresentativeUser(representative);
        link.setRelationshipType(request.relationshipType().name());
        link.setCreatedBy(SYSTEM_USER_ID);
        link.setUpdatedBy(SYSTEM_USER_ID);

        representativeRepository.save(link);

        return dependent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> getDependents(UUID representativeUserId) {
        List<PatientRepresentative> links = representativeRepository
                .findByRepresentativeUserIdAndDeletedAtIsNull(representativeUserId);

        return links.stream()
                .map(PatientRepresentative::getPatient)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getDependentById(UUID dependentId) {
        return patientRepository.findById(dependentId)
                .orElseThrow(() -> new EntityNotFoundException("Dependent not found: " + dependentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientRepresentative getRepresentativeLink(UUID dependentId, UUID representativeUserId) {
        List<PatientRepresentative> links = representativeRepository
                .findByRepresentativeUserIdAndDeletedAtIsNull(representativeUserId);

        return links.stream()
                .filter(link -> link.getPatient().getId().equals(dependentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Representative link not found for dependent: " + dependentId));
    }

    @Override
    @Transactional
    public void removeDependent(UUID dependentId, UUID representativeUserId) {
        PatientRepresentative link = getRepresentativeLink(dependentId, representativeUserId);
        link.setDeletedAt(OffsetDateTime.now());
        link.setUpdatedBy(SYSTEM_USER_ID);
        representativeRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserRepresentativeOf(UUID userId, UUID dependentPatientId) {
        List<PatientRepresentative> links = representativeRepository
                .findByPatientIdAndDeletedAtIsNull(dependentPatientId);

        return links.stream()
                .anyMatch(link -> link.getRepresentativeUser().getId().equals(userId));
    }

    private boolean isAdult(LocalDate birthdate) {
        return birthdate != null && birthdate.plusYears(ADULT_AGE).isBefore(LocalDate.now());
    }
}
