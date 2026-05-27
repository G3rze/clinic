package com.terraplanistas.clinic.domain.dto.response;

import java.util.List;
import java.util.UUID;

public record MedicalRecordResponse(
    UUID id,
    UUID patientId,
    UUID appointmentId,
    UUID employeeId,
    String diagnosisCode,
    String diagnosisDescription,
    String clinicalNotes,
    String physicalExamination,
    List<String> attachments
) {}