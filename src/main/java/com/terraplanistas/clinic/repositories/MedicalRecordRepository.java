package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByPatientId(UUID patientId);
    List<MedicalRecord> findByAppointmentId(UUID appointmentId);
    List<MedicalRecord> findByEmployeeId(UUID employeeId);
    List<MedicalRecord> findByPatientIdAndCreatedAtAfter(UUID patientId, OffsetDateTime since);
}