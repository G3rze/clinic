package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.ConsentRequest;
import com.terraplanistas.clinic.domain.dto.request.PatientProfileRequest;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.UserConsent;

import java.util.UUID;

public interface PatientProfileService {

    Patient getPatientByUserId(UUID userId);

    Patient updateProfile(UUID userId, PatientProfileRequest request);

    boolean isProfileComplete(UUID userId);

    boolean isConsentGiven(UUID userId);

    UserConsent recordConsent(UUID userId, ConsentRequest request);

    String getCurrentConsentVersion();

    boolean isPatientAdult(UUID userId);
}
