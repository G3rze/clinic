package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.CompleteProfileRequest;
import com.terraplanistas.clinic.domain.dto.request.InitRegistrationRequest;
import com.terraplanistas.clinic.domain.dto.response.InitRegistrationResponse;

public interface RegistrationService {

    InitRegistrationResponse initRegistration(InitRegistrationRequest request);

    InitRegistrationResponse submitConsent(String pendingUserConfigId);

    String completeProfile(String pendingUserConfigId, CompleteProfileRequest request, String ipAddress);
}
