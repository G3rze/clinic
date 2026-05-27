package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.PatientRepresentativeRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientRepresentativeResponse;
import com.terraplanistas.clinic.domain.entities.PatientRepresentative;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;

public class PatientRepresentativeMapper {

    public static PatientRepresentative toEntity(PatientRepresentativeRequest request, Patient patient, User representativeUser) {
        PatientRepresentative representative = new PatientRepresentative();
        representative.setPatient(patient);
        representative.setRepresentativeUser(representativeUser);
        representative.setRelationshipType(request.relationshipType());
        return representative;
    }

    public static PatientRepresentative toUpgrade(PatientRepresentativeRequest request, PatientRepresentative representative) {
        representative.setRelationshipType(request.relationshipType());
        return representative;
    }

    public static PatientRepresentativeResponse toResponse(PatientRepresentative representative) {
        return new PatientRepresentativeResponse(
            representative.getId(),
            representative.getPatient() != null ? representative.getPatient().getId() : null,
            representative.getRepresentativeUser() != null ? representative.getRepresentativeUser().getId() : null,
            representative.getRelationshipType(),
            representative.getDeletedAt()
        );
    }
}