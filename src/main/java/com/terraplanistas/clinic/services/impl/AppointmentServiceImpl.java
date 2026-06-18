package com.terraplanistas.clinic.services.impl;

import com.google.api.services.calendar.model.Event;
import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.CreateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentTransactionResponse;
import com.terraplanistas.clinic.domain.dto.response.GoogleEventInfoResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.mapper.AppointmentMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.http.google.GoogleEventsService;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.EmployeeRepository;
import com.terraplanistas.clinic.repositories.EmployeeSpecialtyRepository;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final String PRIMARY_CALENDAR_ID = "primary";
    private static final Set<AppointmentStatus> BLOCKED_STATUSES = Set.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.IN_PROGRESS,
            AppointmentStatus.PENDING_PAYMENT
    );

    private final AppointmentRepository appointmentRepository;
    private final GoogleEventsService googleEventsService;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  GoogleEventsService googleEventsService,
                                  EmployeeRepository employeeRepository,
                                  PatientRepository patientRepository,
                                  UserRepository userRepository,
                                  EmployeeSpecialtyRepository employeeSpecialtyRepository) {
        this.appointmentRepository = appointmentRepository;
        this.googleEventsService = googleEventsService;
        this.employeeRepository = employeeRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.employeeSpecialtyRepository = employeeSpecialtyRepository;
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsForUser(UUID userId, YearMonth month, AppointmentStatus status) {
        OffsetDateTime start = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Appointment> allAppointments = appointmentRepository
                .findByEmployeeUserIdOrPatientUserIdOrPatientCallerUserId(userId, userId, userId);

        List<Appointment> filteredAppointments = allAppointments.stream()
                .filter(a -> a.getExpectedAt() != null)
                .filter(a -> !a.getExpectedAt().isBefore(start) && a.getExpectedAt().isBefore(end))
                .filter(a -> status == null || a.getStatus() == status)
                .sorted((a, b) -> a.getExpectedAt().compareTo(b.getExpectedAt()))
                .collect(Collectors.toList());

        Pageable pageable = Pageable.ofSize(31);
        int total = filteredAppointments.size();

        List<AppointmentResponse> responses = filteredAppointments.stream()
                .map(this::mapToResponseWithEventInfo)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    @Transactional
    public AppointmentTransactionResponse createAppointmentWithPayment(AppointmentTransactionRequest request) {
        CreateAppointmentRequest appointmentInfo = request.appointmentInfo();

        Employee employee = employeeRepository.findById(appointmentInfo.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + appointmentInfo.employeeId()));

        EmployeeSpecialty employeeSpecialty = employeeSpecialtyRepository
                .findByEmployeeIdAndSpecialtyCode(appointmentInfo.employeeId(), appointmentInfo.specialtyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee does not have specialty: " + appointmentInfo.specialtyCode()));

        Patient patient = patientRepository.findById(appointmentInfo.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + appointmentInfo.patientId()));

        User patientCallerUser = userRepository.findById(appointmentInfo.patientCallerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Patient caller user not found: " + appointmentInfo.patientCallerUserId()));

        verifySlotAvailable(appointmentInfo.employeeId(), appointmentInfo.expectedAt());

        Appointment appointment = new Appointment();
        appointment.setGoogleEventId(appointmentInfo.googleEventId());
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setFinalFeePerHour(employeeSpecialty.getFeePerHour());
        appointment.setRegisteredAt(OffsetDateTime.now());
        appointment.setExpectedAt(appointmentInfo.expectedAt());
        appointment.setEmployee(employee);
        appointment.setPatient(patient);
        appointment.setPatientCallerUser(patientCallerUser);

        appointment = appointmentRepository.save(appointment);

        // TODO: Process payment using request.paymentInfo()
        // PaymentResult paymentResult = paymentService.processPayment(request.paymentInfo(), appointment.getId());
        // if (!paymentResult.isSuccess()) {
        //     deleteGoogleCalendarEvent(appointment.getGoogleEventId(), getGoogleUserId(employee));
        //     throw new PaymentFailedException("Payment failed: " + paymentResult.getError());
        // }

        // TODO: Update Google Calendar event to confirmed and send notifications
        // updateGoogleCalendarEventConfirmed(appointment);

        // appointment.setStatus(AppointmentStatus.SCHEDULED);
        // appointment = appointmentRepository.save(appointment);

        AppointmentResponse appointmentResponse = mapToResponseWithEventInfo(appointment);

        return new AppointmentTransactionResponse(appointmentResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse rateAppointment(UUID appointmentId, UUID patientId, RateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Only completed appointments can be rated");
        }

        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessRuleException("Only the patient linked to the appointment can rate it");
        }

        appointment.setScore(request.score());
        appointment.setReview(request.review());

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponseWithEventInfo(saved);
    }

    private void verifySlotAvailable(UUID employeeId, OffsetDateTime expectedAt) {
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByEmployeeIdAndStatusIn(employeeId, List.of(
                        AppointmentStatus.SCHEDULED,
                        AppointmentStatus.IN_PROGRESS,
                        AppointmentStatus.PENDING_PAYMENT));

        boolean slotTaken = conflictingAppointments.stream()
                .anyMatch(a -> a.getExpectedAt().equals(expectedAt));

        if (slotTaken) {
            throw new IllegalStateException("The selected time slot is no longer available");
        }
    }

    private void deleteGoogleCalendarEvent(String eventId, String googleUserId) {
        try {
            googleEventsService.deleteEvent(googleUserId, PRIMARY_CALENDAR_ID, eventId);
        } catch (Exception e) {
            // Log error but don't throw - cleanup should not fail the transaction
        }
    }

    private String getGoogleUserId(Employee employee) {
        if (employee != null && employee.getUser() != null) {
            return employee.getUser().getGoogleUserId();
        }
        return null;
    }

    private AppointmentResponse mapToResponseWithEventInfo(Appointment appointment) {
        GoogleEventInfoResponse eventInfo = fetchGoogleEventInfo(appointment);
        return AppointmentMapper.toResponse(appointment, eventInfo);
    }

    private GoogleEventInfoResponse fetchGoogleEventInfo(Appointment appointment) {
        try {
            String googleUserId = getGoogleUserIdFromAppointment(appointment);
            if (googleUserId == null) {
                return null;
            }
            Event event = googleEventsService.getEventObject(
                    googleUserId,
                    PRIMARY_CALENDAR_ID,
                    appointment.getGoogleEventId()
            );
            return AppointmentMapper.toGoogleEventInfoResponse(event);
        } catch (Exception e) {
            return null;
        }
    }

    private String getGoogleUserIdFromAppointment(Appointment appointment) {
        if (appointment.getEmployee() != null &&
            appointment.getEmployee().getUser() != null) {
            return appointment.getEmployee().getUser().getGoogleUserId();
        }
        return null;
    }
}
