package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.response.DoctorCalendarResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;

import java.time.OffsetDateTime;

public class DoctorCalendarMapper {

    public static DoctorCalendarResponse toCalendarResponse(
            Appointment appointment,
            String meetLink,
            OffsetDateTime endTime
    ) {

        return new DoctorCalendarResponse(
                appointment.getId(),
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
