package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.AddDependentRequest;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.PatientRepresentative;

import java.util.List;
import java.util.UUID;

public interface FamilyService {

    Patient addDependent(UUID representativeUserId, AddDependentRequest request);

    List<Patient> getDependents(UUID representativeUserId);

    Patient getDependentById(UUID dependentId);

    PatientRepresentative getRepresentativeLink(UUID dependentId, UUID representativeUserId);

    void removeDependent(UUID dependentId, UUID representativeUserId);

    boolean isUserRepresentativeOf(UUID userId, UUID dependentPatientId);
}
