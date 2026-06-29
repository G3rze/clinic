package com.terraplanistas.clinic.domain.mapper;

import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.terraplanistas.clinic.domain.dto.request.AppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentParticipantResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.GoogleEventInfoResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;
import java.util.ArrayList;
import java.util.List;

public class AppointmentMapper {

    private static final String PARTICIPANT_TYPE_APPLICATION = "APPLICATION";
    private static final String PARTICIPANT_TYPE_MEET = "MEET";

    public static Appointment toEntity(AppointmentRequest request, Employee employee, Patient patient, User patientCallerUser) {
        Appointment appointment = new Appointment();
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
        appointment.setStatus(request.status());
        appointment.setFinalFeePerHour(request.finalFeePerHour());
        appointment.setScore(request.score());
        appointment.setReview(request.review());
        appointment.setRegisteredAt(request.registeredAt());
        appointment.setExpectedAt(request.expectedAt());
        return appointment;
    }

    public static AppointmentResponse toResponse(Appointment appointment, GoogleEventInfoResponse eventInfo) {
        List<AppointmentParticipantResponse> applicationParticipants = buildApplicationParticipants(appointment);
        List<AppointmentParticipantResponse> meetParticipants = buildMeetParticipants(appointment);

        return new AppointmentResponse(
            appointment.getId(),
            appointment.getStatus(),
            appointment.getFinalFeePerHour(),
            appointment.getScore(),
            appointment.getReview(),
            appointment.getRegisteredAt(),
            appointment.getExpectedAt(),
            appointment.getEmployee() != null ? appointment.getEmployee().getId() : null,
            appointment.getPatient() != null ? appointment.getPatient().getId() : null,
            appointment.getPatientCallerUser() != null ? appointment.getPatientCallerUser().getId() : null,
            eventInfo,
            applicationParticipants,
            meetParticipants,
            buildDoctorName(appointment.getEmployee()),
            buildPatientName(appointment.getPatient()),
            eventInfo != null ? eventInfo.meetLink() : null,
            appointment.getStatus().name()
        );
    }

    private static List<AppointmentParticipantResponse> buildApplicationParticipants(Appointment appointment) {
        List<AppointmentParticipantResponse> participants = new ArrayList<>();
        AppointmentParticipantResponse doctor = buildDoctorParticipant(appointment.getEmployee(), PARTICIPANT_TYPE_APPLICATION);
        AppointmentParticipantResponse patient = buildPatientParticipant(appointment.getPatient());
        if (doctor != null) participants.add(doctor);
        if (patient != null) participants.add(patient);
        return participants;
    }

    private static List<AppointmentParticipantResponse> buildMeetParticipants(Appointment appointment) {
        List<AppointmentParticipantResponse> participants = new ArrayList<>();
        AppointmentParticipantResponse doctor = buildDoctorParticipant(appointment.getEmployee(), PARTICIPANT_TYPE_MEET);
        AppointmentParticipantResponse patientCaller = buildPatientCallerParticipant(appointment.getPatientCallerUser());
        if (doctor != null) participants.add(doctor);
        if (patientCaller != null) participants.add(patientCaller);
        return participants;
    }

    private static AppointmentParticipantResponse buildDoctorParticipant(Employee employee, String participantType) {
        if (employee == null) {
            return null;
        }
        return new AppointmentParticipantResponse(
            employee.getUser() != null ? employee.getUser().getId() : null,
            employee.getFirstName() + " " + employee.getLastName(),
            employee.getUser() != null ? employee.getUser().getEmail() : null,
            "DOCTOR",
            participantType
        );
    }

    private static AppointmentParticipantResponse buildPatientParticipant(Patient patient) {
        if (patient == null) {
            return null;
        }
        return new AppointmentParticipantResponse(
            patient.getUser() != null ? patient.getUser().getId() : null,
            patient.getFirstName() + " " + patient.getLastName(),
            patient.getUser() != null ? patient.getUser().getEmail() : null,
            "PATIENT",
            PARTICIPANT_TYPE_APPLICATION
        );
    }

    private static AppointmentParticipantResponse buildPatientCallerParticipant(User patientCallerUser) {
        if (patientCallerUser == null) {
            return null;
        }
        return new AppointmentParticipantResponse(
            patientCallerUser.getId(),
            patientCallerUser.getUsername() != null ? patientCallerUser.getUsername() : patientCallerUser.getEmail(),
            patientCallerUser.getEmail(),
            "PATIENT_CALLER",
            PARTICIPANT_TYPE_MEET
        );
    }

    public static GoogleEventInfoResponse toGoogleEventInfoResponse(Event event) {
        if (event == null) {
            return null;
        }
        String meetLink = extractMeetLink(event.getConferenceData());
        return new GoogleEventInfoResponse(meetLink);
    }

    public static GoogleEventInfoResponse toGoogleEventInfoResponse(String meetLink) {
        return new GoogleEventInfoResponse(meetLink);
    }

    private static String buildDoctorName(Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getFirstName() + " " + employee.getLastName();
    }

    private static String buildPatientName(Patient patient) {
        if (patient == null) {
            return null;
        }
        return patient.getFirstName() + " " + patient.getLastName();
    }

    private static String extractMeetLink(ConferenceData conferenceData) {
        if (conferenceData == null) {
            return null;
        }
        var entryPoints = conferenceData.getEntryPoints();
        if (entryPoints == null || entryPoints.isEmpty()) {
            return null;
        }
        for (EntryPoint entryPoint : entryPoints) {
            if ("video".equals(entryPoint.getEntryPointType()) || "meet".equals(entryPoint.getEntryPointType())) {
                return entryPoint.getUri();
            }
        }
        return null;
    }
}
