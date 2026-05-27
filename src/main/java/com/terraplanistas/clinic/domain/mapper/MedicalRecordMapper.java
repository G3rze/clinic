package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.MedicalRecord;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;

public class MedicalRecordMapper {

    public static MedicalRecord toEntity(MedicalRecordRequest request, Patient patient, Appointment appointment, Employee employee) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setAppointment(appointment);
        medicalRecord.setEmployee(employee);
        medicalRecord.setDiagnosisCode(request.diagnosisCode());
        medicalRecord.setDiagnosisDescription(request.diagnosisDescription());
        medicalRecord.setClinicalNotes(request.clinicalNotes());
        medicalRecord.setPhysicalExamination(request.physicalExamination());
        medicalRecord.setAttachments(request.attachments());
        return medicalRecord;
    }

    public static MedicalRecord toUpgrade(MedicalRecordRequest request, MedicalRecord medicalRecord) {
        medicalRecord.setDiagnosisCode(request.diagnosisCode());
        medicalRecord.setDiagnosisDescription(request.diagnosisDescription());
        medicalRecord.setClinicalNotes(request.clinicalNotes());
        medicalRecord.setPhysicalExamination(request.physicalExamination());
        medicalRecord.setAttachments(request.attachments());
        return medicalRecord;
    }

    public static MedicalRecordResponse toResponse(MedicalRecord medicalRecord) {
        return new MedicalRecordResponse(
            medicalRecord.getId(),
            medicalRecord.getPatient() != null ? medicalRecord.getPatient().getId() : null,
            medicalRecord.getAppointment() != null ? medicalRecord.getAppointment().getId() : null,
            medicalRecord.getEmployee() != null ? medicalRecord.getEmployee().getId() : null,
            medicalRecord.getDiagnosisCode(),
            medicalRecord.getDiagnosisDescription(),
            medicalRecord.getClinicalNotes(),
            medicalRecord.getPhysicalExamination(),
            medicalRecord.getAttachments()
        );
    }
}