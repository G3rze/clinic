package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.enums.AppointmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DoctorCalendarResponse(
        UUID appointmentId,
        UUID specialtyId,
        String specialtyName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        AppointmentStatus status,
        UUID patientId,
        String patientName,
        String meetLink
) {
}