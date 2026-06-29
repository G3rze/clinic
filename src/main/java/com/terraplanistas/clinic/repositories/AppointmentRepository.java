package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByEmployeeId(UUID employeeId);
    List<Appointment> findByPatientId(UUID patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByEmployeeIdAndStatus(UUID employeeId, AppointmentStatus status);
    List<Appointment> findByEmployeeIdAndStatusIn(UUID employeeId, List<AppointmentStatus> statuses);
    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);
    List<Appointment> findByExpectedAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<Appointment> findByEmployeeUserIdOrPatientUserIdOrPatientCallerUserId(
            UUID employeeUserId, UUID patientUserId, UUID patientCallerUserId);


    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.employee.id = :employeeId
      AND a.status <> :cancelledStatus
      AND a.expectedAt < :newEnd
""")
    List<Appointment> findPotentialConflicts(
            UUID employeeId,
            AppointmentStatus cancelledStatus,
            OffsetDateTime newEnd
    );

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.employee.id = :employeeId
      AND a.expectedAt BETWEEN :from AND :to
      AND a.status <> :cancelledStatus
    ORDER BY a.expectedAt
""")
    List<Appointment> findDoctorCalendar(
            UUID employeeId,
            OffsetDateTime from,
            OffsetDateTime to,
            AppointmentStatus cancelledStatus
    );

    Optional<Appointment> findByReceiptId(UUID receiptId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.expectedAt >= :start AND a.expectedAt < :end")
    long countByExpectedAtBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);



}