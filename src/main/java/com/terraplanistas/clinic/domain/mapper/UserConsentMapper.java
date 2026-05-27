package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.UserConsentRequest;
import com.terraplanistas.clinic.domain.dto.response.UserConsentResponse;
import com.terraplanistas.clinic.domain.entities.UserConsent;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.entities.Patient;

public class UserConsentMapper {

    public static UserConsent toEntity(UserConsentRequest request, User user, Patient patient) {
        UserConsent userConsent = new UserConsent();
        userConsent.setUserId(request.userId());
        userConsent.setUser(user);
        userConsent.setVersion(request.version());
        userConsent.setPatientId(request.patientId());
        userConsent.setPatient(patient);
        userConsent.setTreatmentPurpose(request.treatmentPurpose());
        userConsent.setDataAnalysisPurpose(request.dataAnalysisPurpose());
        return userConsent;
    }

    public static UserConsent toUpgrade(UserConsentRequest request, UserConsent userConsent) {
        userConsent.setTreatmentPurpose(request.treatmentPurpose());
        userConsent.setDataAnalysisPurpose(request.dataAnalysisPurpose());
        return userConsent;
    }

    public static UserConsentResponse toResponse(UserConsent userConsent) {
        return new UserConsentResponse(
            userConsent.getUserId(),
            userConsent.getVersion(),
            userConsent.getPatientId(),
            userConsent.getTreatmentPurpose(),
            userConsent.getDataAnalysisPurpose(),
            userConsent.getConsentedAt()
        );
    }
}