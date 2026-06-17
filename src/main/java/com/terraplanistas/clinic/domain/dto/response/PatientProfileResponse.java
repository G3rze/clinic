package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.Patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientProfileResponse(
    UUID id,
    String firstName,
    String lastName,
    String idNumber,
    String idType,
    String address,
    String phones,
    LocalDate birthdate,
    boolean isComplete,
    boolean isAdult,
    boolean hasConsent
) {
    public static PatientProfileResponse from(Patient patient, boolean isComplete, boolean isAdult, boolean hasConsent) {
        return new PatientProfileResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getIdNumber(),
                patient.getIdType().name(),
                patient.getAddress(),
                patient.getPhones(),
                patient.getBirthdate(),
                isComplete,
                isAdult,
                hasConsent
        );
    }
}