package com.terraplanistas.clinic.domain.mapper;

import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.terraplanistas.clinic.domain.dto.request.AppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentParticipantResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.GoogleEventInfoResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class AppointmentMapper {

    private static final String PARTICIPANT_TYPE_APPLICATION = "APPLICATION";
    private static final String PARTICIPANT_TYPE_MEET = "MEET";

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

    public static AppointmentResponse toResponse(Appointment appointment, GoogleEventInfoResponse eventInfo) {
        List<AppointmentParticipantResponse> applicationParticipants = buildApplicationParticipants(appointment);
        List<AppointmentParticipantResponse> meetParticipants = buildMeetParticipants(appointment);

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
            appointment.getPatientCallerUser() != null ? appointment.getPatientCallerUser().getId() : null,
            eventInfo,
            applicationParticipants,
            meetParticipants
        );
    }

    private static List<AppointmentParticipantResponse> buildApplicationParticipants(Appointment appointment) {
        AppointmentParticipantResponse doctor = buildDoctorParticipant(appointment.getEmployee(), PARTICIPANT_TYPE_APPLICATION);
        AppointmentParticipantResponse patient = buildPatientParticipant(appointment.getPatient());
        return List.of(doctor, patient);
    }

    private static List<AppointmentParticipantResponse> buildMeetParticipants(Appointment appointment) {
        AppointmentParticipantResponse doctor = buildDoctorParticipant(appointment.getEmployee(), PARTICIPANT_TYPE_MEET);
        AppointmentParticipantResponse patientCaller = buildPatientCallerParticipant(appointment.getPatientCallerUser());
        return List.of(doctor, patientCaller);
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

        OffsetDateTime startTime = parseEventDateTime(event.getStart());
        OffsetDateTime endTime = parseEventDateTime(event.getEnd());
        String meetLink = extractMeetLink(event.getConferenceData());

        return new GoogleEventInfoResponse(
            event.getId(),
            event.getSummary(),
            event.getDescription(),
            event.getLocation(),
            startTime,
            endTime,
            meetLink
        );
    }

    private static OffsetDateTime parseEventDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return null;
        }
        if (eventDateTime.getDateTime() != null) {
            com.google.api.client.util.DateTime dateTime = eventDateTime.getDateTime();
            int tzShift = dateTime.getTimeZoneShift();
            return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dateTime.getValue()),
                tzShift == 0 ? ZoneOffset.UTC : ZoneOffset.ofTotalSeconds(tzShift * 60)
            );
        }
        if (eventDateTime.getDate() != null) {
            return LocalDate.parse(eventDateTime.getDate().toString()).atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        return null;
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
