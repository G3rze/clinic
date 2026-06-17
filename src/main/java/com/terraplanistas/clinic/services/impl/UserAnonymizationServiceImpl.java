package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.response.PermanentlyAnonymizedAuditResponse;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.UserAnonymizationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserAnonymizationServiceImpl implements UserAnonymizationService {

    private static final Logger log = LoggerFactory.getLogger(UserAnonymizationServiceImpl.class);
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public UserAnonymizationServiceImpl(UserRepository userRepository,
                                         PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public void anonymizeUser(UUID userId, UUID performedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (user.getRole() != null && "EMPLOYEE".equals(user.getRole().getCode())) {
            throw new IllegalArgumentException("Employees cannot be anonymized, only access can be revoked");
        }

        user.setEmail("DELETED_" + user.getId() + "@anonymized.local");
        user.setUsername("DELETED");
        user.setGoogleUserId(null);
        user.setDeletedAt(OffsetDateTime.now());
        user.setDeletedBy(performedBy != null ? performedBy : SYSTEM_USER_ID);
        user.setUpdatedBy(performedBy != null ? performedBy : SYSTEM_USER_ID);

        userRepository.save(user);

        patientRepository.findByUserId(userId).ifPresent(patient -> {
            patient.setFirstName("DELETED");
            patient.setLastName("DELETED");
            patient.setIdNumber("DELETED");
            patient.setAddress("DELETED");
            patient.setUpdatedBy(SYSTEM_USER_ID);
            patientRepository.save(patient);
        });

        log.info("User anonymized: {}", userId);
    }

    @Override
    @Transactional
    public void markAnonymizationPermanent(int days) {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(days);
        List<User> toMark = userRepository.findAnonymizedBeforeAndNotYetPermanent(threshold);

        if (toMark.isEmpty()) {
            log.info("No anonymized users older than {} days to mark as permanent", days);
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (User user : toMark) {
            user.setAnonymizationPermanentAt(now);
        }
        userRepository.saveAll(toMark);

        log.info("Marked {} anonymized users as permanent", toMark.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAnonymized(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getEmail() != null && user.getEmail().startsWith("DELETED_"))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermanentlyAnonymizedAuditResponse> getPermanentlyAnonymized(
            Pageable pageable,
            OffsetDateTime deletedAtFrom,
            OffsetDateTime deletedAtTo,
            OffsetDateTime permanentAtFrom,
            OffsetDateTime permanentAtTo) {
        return userRepository.findPermanentlyAnonymized(
                deletedAtFrom, deletedAtTo, permanentAtFrom, permanentAtTo, pageable)
                .map(PermanentlyAnonymizedAuditResponse::from);
    }
}
