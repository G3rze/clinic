package com.terraplanistas.clinic.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terraplanistas.clinic.domain.dto.request.CompleteProfileRequest;
import com.terraplanistas.clinic.domain.dto.request.InitRegistrationRequest;
import com.terraplanistas.clinic.domain.dto.response.InitRegistrationResponse;
import com.terraplanistas.clinic.domain.encryption.AESEncryptionService;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.PendingUserConfig;
import com.terraplanistas.clinic.domain.entities.Role;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.entities.UserConsent;
import com.terraplanistas.clinic.http.security.JwtTokenService;
import com.terraplanistas.clinic.http.security.SecurityProperties;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.PendingUserConfigRepository;
import com.terraplanistas.clinic.repositories.RoleRepository;
import com.terraplanistas.clinic.repositories.UserConsentRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.RegistrationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final int ADULT_AGE = 18;
    private static final String DEFAULT_ROLE_CODE = "USER";
    private static final int PENDING_CONFIG_TTL_MINUTES = 30;

    private final PendingUserConfigRepository pendingUserConfigRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final UserConsentRepository userConsentRepository;
    private final JwtTokenService jwtTokenService;
    private final SecurityProperties securityProperties;
    private final AESEncryptionService encryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegistrationServiceImpl(PendingUserConfigRepository pendingUserConfigRepository,
                                   UserRepository userRepository,
                                   PatientRepository patientRepository,
                                   RoleRepository roleRepository,
                                   UserConsentRepository userConsentRepository,
                                   JwtTokenService jwtTokenService,
                                   SecurityProperties securityProperties,
                                   AESEncryptionService encryptionService) {
        this.pendingUserConfigRepository = pendingUserConfigRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.userConsentRepository = userConsentRepository;
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
        this.encryptionService = encryptionService;
    }

    @Override
    @Transactional
    public InitRegistrationResponse initRegistration(InitRegistrationRequest request) {
        boolean isAdult = isAdult(request.birthdate());

        if (!isAdult) {
            return new InitRegistrationResponse(
                    null,
                    false,
                    "You must be at least 18 years old to register. An adult must complete this process."
            );
        }

        PendingUserConfig config = pendingUserConfigRepository.findByGoogleUserId(request.googleUserId())
                .orElse(new PendingUserConfig());

        config.setGoogleUserId(request.googleUserId());
        config.setEmail(request.email());
        config.setName(request.name());
        config.setBirthdate(request.birthdate());
        config.setCreatedAt(OffsetDateTime.now());
        config.setConsentGiven(false);
        config.setProfileComplete(false);

        config = pendingUserConfigRepository.save(config);

        return new InitRegistrationResponse(
                config.getId(),
                true,
                "Registration initialized. Please complete consent to continue."
        );
    }

    @Override
    @Transactional
    public InitRegistrationResponse submitConsent(String pendingUserConfigId) {
        PendingUserConfig config = pendingUserConfigRepository.findById(UUID.fromString(pendingUserConfigId))
                .orElseThrow(() -> new EntityNotFoundException("Pending user config not found"));

        if (config.isExpired(PENDING_CONFIG_TTL_MINUTES)) {
            pendingUserConfigRepository.delete(config);
            throw new IllegalArgumentException("Registration session has expired. Please start again.");
        }

        config.setConsentGiven(true);
        pendingUserConfigRepository.save(config);

        return new InitRegistrationResponse(
                config.getId(),
                true,
                "Consent recorded. Please complete your profile to finish registration."
        );
    }

    @Override
    @Transactional
    public String completeProfile(String pendingUserConfigId, CompleteProfileRequest request, String ipAddress) {
        PendingUserConfig config = pendingUserConfigRepository.findById(UUID.fromString(pendingUserConfigId))
                .orElseThrow(() -> new EntityNotFoundException("Pending user config not found or expired"));

        if (config.isExpired(PENDING_CONFIG_TTL_MINUTES)) {
            pendingUserConfigRepository.delete(config);
            throw new IllegalArgumentException("Registration session has expired. Please start again.");
        }

        if (!config.isConsentGiven()) {
            throw new IllegalArgumentException("Consent must be given before completing profile.");
        }

        if (config.getBirthdate() == null) {
            throw new IllegalArgumentException("Birthdate is required. Please restart the registration process.");
        }

        User user = userRepository.findByGoogleUserId(config.getGoogleUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Patient patient = createPatient(user, request, config.getBirthdate());
        recordConsent(user, patient);

        pendingUserConfigRepository.delete(config);

        String accountStatus = "active";
        String requiresAction = "";

        List<String> roles = user.getRole() != null ? List.of(user.getRole().getCode()) : List.of("USER");

        String jwtAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String jwtRefreshToken = jwtTokenService.generateRefreshToken(user.getId());

        try {
            return objectMapper.writeValueAsString(Map.of(
                    "access_token", jwtAccessToken,
                    "refresh_token", jwtRefreshToken,
                    "token_type", "Bearer",
                    "expires_in", jwtTokenService.getAccessTokenExpirationMs() / 1000,
                    "is_new_user", true,
                    "account_status", accountStatus,
                    "requires_action", requiresAction,
                    "user", Map.of(
                            "id", user.getId().toString(),
                            "google_user_id", user.getGoogleUserId(),
                            "name", user.getUsername() != null ? user.getUsername() : "",
                            "email", user.getEmail() != null ? user.getEmail() : "",
                            "roles", roles,
                            "account_status", accountStatus,
                            "requires_action", requiresAction
                    )
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize registration response", e);
        }
    }

    private boolean isAdult(LocalDate birthdate) {
        return birthdate != null && birthdate.plusYears(ADULT_AGE).isBefore(LocalDate.now());
    }

    private User createUser(PendingUserConfig config) {
        Role userRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        User user = new User();
        user.setGoogleUserId(config.getGoogleUserId());
        user.setEmail(config.getEmail());
        user.setUsername(config.getName() != null ? config.getName() : "User");
        user.setRole(userRole);

        if (config.getEmail() != null) {
            user.setEmailBindex(encryptionService.encryptDeterministic(config.getEmail().toLowerCase()));
        }
        if (config.getName() != null) {
            user.setUsernameBindex(encryptionService.encryptDeterministic(config.getName().toLowerCase()));
        }

        return userRepository.save(user);
    }

    private Patient createPatient(User user, CompleteProfileRequest request, LocalDate birthdate) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setIdType(request.idType());
        patient.setIdNumber(request.idNumber());
        patient.setAddress(request.address());
        patient.setPhones(request.phones());
        patient.setBirthdate(birthdate);
        patient.setIsActive(true);

        patient.setFirstNameBindex(encryptionService.encryptDeterministic(request.firstName().toLowerCase()));
        patient.setLastNameBindex(encryptionService.encryptDeterministic(request.lastName().toLowerCase()));
        patient.setIdNumberBindex(encryptionService.encryptDeterministic(request.idNumber()));

        return patientRepository.save(patient);
    }

    private void recordConsent(User user, Patient patient) {
        String version = securityProperties.getConsent().getVersion();

        UserConsent consent = new UserConsent();
        consent.setUserId(user.getId());
        consent.setUser(user);
        consent.setPatientId(patient.getId());
        consent.setPatient(patient);
        consent.setVersion(version);
        consent.setTreatmentPurpose(true);
        consent.setDataAnalysisPurpose(true);
        consent.setConsentedAt(OffsetDateTime.now());

        userConsentRepository.save(consent);
    }
}
