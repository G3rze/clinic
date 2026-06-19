package com.terraplanistas.clinic.services.impl;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.CreateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.request.DoctorCalendarRequest;
import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.*;
import com.terraplanistas.clinic.domain.entities.*;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.enums.PaymentStatus;
import com.terraplanistas.clinic.domain.mapper.AppointmentMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.http.google.GoogleEventsService;
import com.terraplanistas.clinic.http.stripe.StripePaymentService;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentCreateRequest;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentResponse;
import com.terraplanistas.clinic.http.stripe.dto.RefundRequest;
import com.terraplanistas.clinic.repositories.*;
import com.terraplanistas.clinic.services.AppointmentService;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
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
    private final StripePaymentService stripePaymentService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final ReceiptRepository receiptRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  GoogleEventsService googleEventsService,
                                  EmployeeRepository employeeRepository,
                                  PatientRepository patientRepository,
                                  UserRepository userRepository,
                                  EmployeeSpecialtyRepository employeeSpecialtyRepository, StripePaymentService stripePaymentService, DoctorAvailabilityRepository doctorAvailabilityRepository, AppointmentMapper appointmentMapper, ReceiptRepository receiptRepository) {
        this.appointmentRepository = appointmentRepository;
        this.googleEventsService = googleEventsService;
        this.employeeRepository = employeeRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.employeeSpecialtyRepository = employeeSpecialtyRepository;
        this.stripePaymentService = stripePaymentService;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.receiptRepository = receiptRepository;
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
    public AppointmentTransactionResponse createAppointmentWithPayment(
            AppointmentTransactionRequest request
    ) {

        CreateAppointmentRequest appointmentInfo =
                request.appointmentInfo();

        Employee employee =
                employeeRepository.findById(
                        appointmentInfo.employeeId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        Patient patient =
                patientRepository.findById(
                        appointmentInfo.patientId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        User patientCaller =
                userRepository.findById(
                        appointmentInfo.patientCallerUserId()
                ).orElseThrow(() ->
                         new ResourceNotFoundException(
                "Employee not found"
        )
                );

        EmployeeSpecialty employeeSpecialty =
                employeeSpecialtyRepository
                        .findByEmployeeIdAndSpecialtyCode(
                                employee.getId(),
                                appointmentInfo.specialtyCode()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found"
                                )
                        );

        OffsetDateTime start =
                appointmentInfo.expectedAt();

        OffsetDateTime end =
                start.plusMinutes(
                        employeeSpecialty.getConsultDurationMinutes()
                );

        verifyAvailability(
                employee.getId(),
                employeeSpecialty,
                appointmentInfo.expectedAt()
        );

        String googleUserId =
                employee.getUser().getGoogleUserId();

        if (googleUserId == null || googleUserId.isBlank()) {
            throw new BusinessRuleException(
                    "Doctor Google account not linked"
            );
        }

        Event event = new Event();

        event.setSummary(
                "Medical Appointment"
        );

        event.setDescription(
                "Consultation"
        );

        event.setStart(
                new EventDateTime()
                        .setDateTime(
                                new DateTime(
                                        start.toInstant()
                                                .toEpochMilli()
                                )
                        )
        );

        event.setEnd(
                new EventDateTime()
                        .setDateTime(
                                new DateTime(
                                        end.toInstant()
                                                .toEpochMilli()
                                )
                        )
        );

        String googleEventId =
                googleEventsService.createMeetConference(
                        googleUserId,
                        "primary",
                        event
                );

        Event googleEvent =
                googleEventsService.getEventObject(
                        googleUserId,
                        "primary",
                        googleEventId
                );

        GoogleEventInfoResponse eventInfo =
                AppointmentMapper.toGoogleEventInfoResponse(
                        googleEvent
                );

        BigDecimal amount =
                employeeSpecialty.getFeePerHour();

        PaymentIntentResponse paymentIntent =
                stripePaymentService.createPaymentIntent(
                        PaymentIntentCreateRequest.builder()
                                .amount(amount)
                                .currency(
                                        request.paymentInfo()
                                                .currency()
                                )
                                .metadata(
                                        Map.of(
                                                "employee_id",
                                                employee.getId().toString(),
                                                "patient_id",
                                                patient.getId().toString()
                                        )
                                )
                                .build()
                );

        Receipt receipt = new Receipt();

        receipt.setAmount(
                amount
        );

        receipt.setPaymentStatus(
                PaymentStatus.PENDING
        );

        receipt.setTransactionId(
                paymentIntent.id()
        );

        receipt =
                receiptRepository.save(
                        receipt
                );

        Appointment appointment =
                new Appointment();

        appointment.setGoogleEventId(
                googleEventId
        );

        appointment.setStatus(
                AppointmentStatus.PENDING_PAYMENT
        );

        appointment.setFinalFeePerHour(
                employeeSpecialty.getFeePerHour()
        );

        appointment.setRegisteredAt(
                OffsetDateTime.now()
        );

        appointment.setExpectedAt(
                start
        );

        appointment.setEmployee(
                employee
        );

        appointment.setPatient(
                patient
        );

        appointment.setPatientCallerUser(
                patientCaller
        );

        appointment.setReceipt(
                receipt
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        AppointmentResponse appointmentResponse =
                AppointmentMapper.toResponse(
                        appointment,
                        eventInfo
                );

        return new AppointmentTransactionResponse(
                appointmentResponse,
                paymentIntent.id(),
                paymentIntent.status(),
                paymentIntent.clientSecret(),
                paymentIntent.amount()
        );
    }

    @Transactional
    public AppointmentCancellationResponse cancelAppointment(
            UUID appointmentId
    ) {

        Appointment appointment =
                appointmentRepository.findById(
                        appointmentId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );



        if (
                appointment.getStatus()
                        == AppointmentStatus.CANCELLED
        ) {
            throw new BusinessRuleException(
                    "Appointment already cancelled"
            );
        }

        if (
                appointment.getExpectedAt()
                        .isBefore(
                                OffsetDateTime.now()
                                        .plusHours(4)
                        )
        ) {
            throw new BusinessRuleException(
                    "Appointments can only be cancelled at least 4 hours before"
            );
        }

        Receipt receipt = getReceipt(appointment);

        if (receipt.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException(
                    "Only paid appointments can be refunded"
            );
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException(
                    "Only scheduled appointments can be cancelled"
            );
        }



        BigDecimal refundAmount =
                receipt.getAmount()
                        .multiply(
                                BigDecimal.valueOf(0.80)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        stripePaymentService.refund(
                new RefundRequest(
                        receipt.getTransactionId(),
                        refundAmount
                )
        );

        String googleUserId =
                appointment.getEmployee()
                        .getUser()
                        .getGoogleUserId();

        googleEventsService.deleteEvent(
                googleUserId,
                "primary",
                appointment.getGoogleEventId()
        );

        receipt.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        receiptRepository.save(
                receipt
        );

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointmentRepository.save(
                appointment
        );

        return new AppointmentCancellationResponse(
                appointment.getId(),
                appointment.getStatus().name(),
                refundAmount
        );
    }

    private static @NonNull Receipt getReceipt(Appointment appointment) {
        Receipt receipt =
                appointment.getReceipt();

        if (receipt == null) {
            throw new BusinessRuleException(
                    "Appointment has no receipt"
            );
        }

        return receipt;
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
            throw new BusinessRuleException("The selected time slot is no longer available");
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

    private void verifyAvailability(
            UUID employeeId,
            EmployeeSpecialty employeeSpecialty,
            OffsetDateTime requestedStart
    ) {

        OffsetDateTime requestedEnd =
                requestedStart.plusMinutes(
                        employeeSpecialty.getConsultDurationMinutes()
                );

        List<Appointment> appointments =
                appointmentRepository.findPotentialConflicts(
                        employeeId,
                        AppointmentStatus.CANCELLED,
                        requestedEnd
                );

        for (Appointment appointment : appointments) {

            OffsetDateTime existingStart =
                    appointment.getExpectedAt();

            OffsetDateTime existingEnd =
                    existingStart.plusMinutes(
                            employeeSpecialty.getConsultDurationMinutes()
                    );

            boolean overlaps =
                    requestedStart.isBefore(existingEnd)
                            && requestedEnd.isAfter(existingStart);

            if (overlaps) {
                throw new BusinessRuleException(
                        "The selected appointment slot is already occupied"
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<DoctorCalendarResponse> getDoctorCalendar(
            UUID employeeId,
            DoctorCalendarRequest request
    ) {

        EmployeeSpecialty specialty =
                employeeSpecialtyRepository.findById(
                        new EmployeeSpecialtyId(
                                employeeId,
                                request.specialtyId()
                        )
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        List<Appointment> appointments =
                appointmentRepository.findDoctorCalendar(
                        employeeId,
                        request.from(),
                        request.to(),
                        AppointmentStatus.CANCELLED
                );

        return appointments.stream()
                .map(appointment -> {

                    OffsetDateTime endTime =
                            appointment.getExpectedAt()
                                    .plusMinutes(
                                            specialty.getConsultDurationMinutes()
                                    );

                    String meetLink = null;

                    try {

                        String googleUserId =
                                appointment.getEmployee()
                                        .getUser()
                                        .getGoogleUserId();

                        Event event =
                                googleEventsService.getEventObject(
                                        googleUserId,
                                        "primary",
                                        appointment.getGoogleEventId()
                                );

                        GoogleEventInfoResponse eventInfo =
                                AppointmentMapper
                                        .toGoogleEventInfoResponse(
                                                event
                                        );

                        meetLink =
                                eventInfo != null
                                        ? eventInfo.meetLink()
                                        : null;

                    } catch (Exception ignored) {
                    }

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
                })
                .toList();
    }
}
