package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.PatientRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;

public class PatientMapper {

    public static Patient toEntity(PatientRequest request, User user) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setIdNumber(request.idNumber());
        patient.setIdType(request.idType());
        patient.setAddress(request.address());
        patient.setPhones(request.phones());
        patient.setBirthdate(request.birthdate());
        patient.setIsActive(true);
        return patient;
    }

    public static Patient toUpgrade(PatientRequest request, Patient patient) {
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setIdNumber(request.idNumber());
        patient.setIdType(request.idType());
        patient.setAddress(request.address());
        patient.setPhones(request.phones());
        patient.setBirthdate(request.birthdate());
        return patient;
    }

    public static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getIdNumber(),
            patient.getIdType(),
            patient.getAddress(),
            patient.getPhones(),
            patient.getIsActive(),
            patient.getBirthdate(),
            patient.getUser() != null ? patient.getUser().getId() : null
        );
    }
}