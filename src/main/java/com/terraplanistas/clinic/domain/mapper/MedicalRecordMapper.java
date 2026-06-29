package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.MedicalRecord;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;

import java.util.UUID;

public class MedicalRecordMapper {

    public static MedicalRecord toEntity(MedicalRecordRequest request, Patient patient, Appointment appointment, Employee employee, UUID createdBy) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setAppointment(appointment);
        medicalRecord.setEmployee(employee);
        medicalRecord.setDiagnosisCode(request.diagnosisCode());
        medicalRecord.setDiagnosisDescription(request.diagnosisDescription());
        medicalRecord.setClinicalNotes(request.clinicalNotes());
        medicalRecord.setPhysicalExamination(request.physicalExamination());
        medicalRecord.setAttachments(request.attachments());
        medicalRecord.setCreatedBy(createdBy);
        medicalRecord.setUpdatedBy(createdBy);
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
        String doctorName = null;
        if (medicalRecord.getEmployee() != null) {
            Employee emp = medicalRecord.getEmployee();
            doctorName = emp.getFirstName() + " " + emp.getLastName();
        }

        return new MedicalRecordResponse(
            medicalRecord.getId(),
            medicalRecord.getPatient() != null ? medicalRecord.getPatient().getId() : null,
            medicalRecord.getAppointment() != null ? medicalRecord.getAppointment().getId() : null,
            medicalRecord.getEmployee() != null ? medicalRecord.getEmployee().getId() : null,
            doctorName,
            medicalRecord.getCreatedAt(),
            medicalRecord.getDiagnosisCode(),
            medicalRecord.getDiagnosisDescription(),
            medicalRecord.getClinicalNotes(),
            medicalRecord.getPhysicalExamination(),
            medicalRecord.getAttachments()
        );
    }
}