package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordService {
    MedicalRecordResponse registerRecord(UUID doctor, MedicalRecordRequest request);
    List<MedicalRecordResponse> getFromAppointmentId(UUID requester, UUID appointmentId);
    List<MedicalRecordResponse> getByPatientId(UUID patientId);
    List<MedicalRecordResponse> getByPatientIdAndFilters(UUID patientId, OffsetDateTime fromDate, OffsetDateTime toDate, UUID doctorId);
}
