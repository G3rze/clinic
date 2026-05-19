package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByGoogleEventId(String googleEventId);
    List<Appointment> findByEmployeeId(UUID employeeId);
    List<Appointment> findByPatientId(UUID patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByEmployeeIdAndStatus(UUID employeeId, AppointmentStatus status);
    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);
    List<Appointment> findByExpectedAtBetween(OffsetDateTime start, OffsetDateTime end);
}