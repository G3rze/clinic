package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.AppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;

public class AppointmentMapper {

    public static Appointment toEntity(AppointmentRequest request, Employee employee, Patient patient, User patientCallerUser) {
        Appointment appointment = new Appointment();
        appointment.setGoogleEventId(request.googleEventId());
        appointment.setStatus(request.status());
        appointment.setFinalFeePerHour(request.finalFeePerHour());
        appointment.setScore(request.score());
        appointment.setReview(request.review());
        appointment.setRegisteredAt(request.registeredAt());
        appointment.setExpectedAt(request.expectedAt());
        appointment.setEmployee(employee);
        appointment.setPatient(patient);
        appointment.setPatientCallerUser(patientCallerUser);
        return appointment;
    }

    public static Appointment toUpgrade(AppointmentRequest request, Appointment appointment) {
        appointment.setGoogleEventId(request.googleEventId());
        appointment.setStatus(request.status());
        appointment.setFinalFeePerHour(request.finalFeePerHour());
        appointment.setScore(request.score());
        appointment.setReview(request.review());
        appointment.setRegisteredAt(request.registeredAt());
        appointment.setExpectedAt(request.expectedAt());
        return appointment;
    }

    public static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getGoogleEventId(),
            appointment.getStatus(),
            appointment.getFinalFeePerHour(),
            appointment.getScore(),
            appointment.getReview(),
            appointment.getRegisteredAt(),
            appointment.getExpectedAt(),
            appointment.getEmployee() != null ? appointment.getEmployee().getId() : null,
            appointment.getPatient() != null ? appointment.getPatient().getId() : null,
            appointment.getPatientCallerUser() != null ? appointment.getPatientCallerUser().getId() : null
        );
    }
}