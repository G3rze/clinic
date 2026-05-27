package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record MedicalRecordRequest(
    @NotNull(message = "Patient ID is required")
    UUID patientId,

    @NotNull(message = "Appointment ID is required")
    UUID appointmentId,

    @NotNull(message = "Employee ID is required")
    UUID employeeId,

    @NotBlank(message = "Diagnosis code is required")
    String diagnosisCode,

    @NotBlank(message = "Diagnosis description is required")
    String diagnosisDescription,

    @NotBlank(message = "Clinical notes are required")
    String clinicalNotes,

    String physicalExamination,

    List<String> attachments
) {}