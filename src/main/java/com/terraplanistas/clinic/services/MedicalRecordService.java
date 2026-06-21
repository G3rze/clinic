package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordService {
    public MedicalRecordResponse registerRecord(UUID doctor, MedicalRecordRequest request);
    public List<MedicalRecordResponse> getFromAppointmentId(UUID requester, UUID appointmentId);
}
