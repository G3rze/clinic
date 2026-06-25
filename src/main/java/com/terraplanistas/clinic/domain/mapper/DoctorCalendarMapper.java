package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.response.DoctorCalendarResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;

import java.time.OffsetDateTime;

public class DoctorCalendarMapper {

    public static DoctorCalendarResponse toCalendarResponse(
            Appointment appointment,
            EmployeeSpecialty specialty,
            String meetLink,
            OffsetDateTime endTime
    ) {
        return new DoctorCalendarResponse(
                appointment.getId(),
                specialty.getSpecialtyId(),
                specialty.getSpecialty().getName(),
                appointment.getExpectedAt(),
                endTime,
                appointment.getStatus(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName()
                        + " "
                        + appointment.getPatient().getLastName(),
                meetLink
        );
    }

}
