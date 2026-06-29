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
import com.terraplanistas.clinic.http.config.AppStripeProperties;
import com.terraplanistas.clinic.http.stripe.StripePaymentService;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentCreateRequest;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentResponse;
import com.terraplanistas.clinic.http.stripe.dto.RefundRequest;
import com.terraplanistas.clinic.repositories.*;
import com.terraplanistas.clinic.services.AppointmentService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final GoogleEventsService googleEventsService;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;
    private final PatientRepresentativeRepository patientRepresentativeRepository;
    private final DataSource dataSource;
    private final StripePaymentService stripePaymentService;
    private final ReceiptRepository receiptRepository;
    private final AppStripeProperties appStripeProperties;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  GoogleEventsService googleEventsService,
                                  EmployeeRepository employeeRepository,
                                  PatientRepository patientRepository,
                                  UserRepository userRepository,
                                  EmployeeSpecialtyRepository employeeSpecialtyRepository,
                                  PatientRepresentativeRepository patientRepresentativeRepository,
                                  StripePaymentService stripePaymentService,
                                  ReceiptRepository receiptRepository,
                                  DataSource dataSource,
                                  AppStripeProperties appStripeProperties) {
        this.appointmentRepository = appointmentRepository;
        this.googleEventsService = googleEventsService;
        this.employeeRepository = employeeRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.employeeSpecialtyRepository = employeeSpecialtyRepository;
        this.patientRepresentativeRepository = patientRepresentativeRepository;
        this.stripePaymentService = stripePaymentService;
        this.receiptRepository = receiptRepository;
        this.dataSource = dataSource;
        this.appStripeProperties = appStripeProperties;
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsForUser(UUID userId, YearMonth month, AppointmentStatus status) {
        List<Appointment> allAppointments = appointmentRepository
                .findByEmployeeUserIdOrPatientUserIdOrPatientCallerUserId(userId, userId, userId);

        List<Appointment> filteredAppointments = allAppointments.stream()
                .filter(a -> a.getExpectedAt() != null)
                .filter(a -> month == null || (!a.getExpectedAt().isBefore(month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)) && a.getExpectedAt().isBefore(month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC))))
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

        log.info("=== DIAGNOSTIC ===");
        log.info("employeeId received: {}", appointmentInfo.employeeId());
        log.info("patientId received: {}", appointmentInfo.patientId());
        try {
            log.info("Database URL: {}", dataSource.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            log.error("Could not get DB URL", e);
        }

        Employee employee =
                employeeRepository.findById(
                        appointmentInfo.employeeId()
                ).orElseThrow(() -> {
                        log.error("Employee NOT FOUND in DB for ID: {}", appointmentInfo.employeeId());
                        return new ResourceNotFoundException(
                                "Employee not found with ID: " + appointmentInfo.employeeId()
                        );
                }
        );

        Patient patient =
                patientRepository.findById(
                        appointmentInfo.patientId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with ID: " + appointmentInfo.patientId()
                        )
                );

        User patientCaller;
        UUID patientCallerUserId = appointmentInfo.patientCallerUserId();
        if (patientCallerUserId != null) {
            Optional<User> callerUser = userRepository.findById(patientCallerUserId);
            if (callerUser.isPresent()) {
                patientCaller = callerUser.get();
            } else if (patient.getUser() != null) {
                log.warn("patientCallerUserId {} not found, falling back to patient.user.id ({})",
                        patientCallerUserId, patient.getUser().getId());
                patientCaller = patient.getUser();
            } else {
                List<PatientRepresentative> representatives = patientRepresentativeRepository
                        .findByPatientIdAndDeletedAtIsNull(patient.getId());
                if (representatives.isEmpty()) {
                    throw new ResourceNotFoundException(
                            "User not found with ID: " + patientCallerUserId
                    );
                }
                patientCaller = representatives.get(0).getRepresentativeUser();
                log.warn("patientCallerUserId {} and patient.user both not found, "
                                + "falling back to PatientRepresentative.representativeUser ({}) for minor patient {}",
                        patientCallerUserId, patientCaller.getId(), patient.getId());
            }
        } else if (patient.getUser() != null) {
            patientCaller = patient.getUser();
        } else {
            List<PatientRepresentative> representatives = patientRepresentativeRepository
                    .findByPatientIdAndDeletedAtIsNull(patient.getId());
            if (representatives.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Cannot determine patient caller: patient has no user link and no representative on record"
                );
            }
            patientCaller = representatives.get(0).getRepresentativeUser();
            log.warn("patientCallerUserId was null, resolved via PatientRepresentative.representativeUser ({}) for minor patient {}",
                    patientCaller.getId(), patient.getId());
        }

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

        String patientGoogleUserId = patient.getUser().getGoogleUserId();

        if (patientGoogleUserId == null || patientGoogleUserId.isBlank()) {
            throw new BusinessRuleException(
                    "Patient Google account not linked - cannot create Meet link"
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

        GoogleEventInfoResponse eventInfo = null;
        String googleEventId = null;
        try {
            googleEventId =
                    googleEventsService.createMeetConference(
                            patientGoogleUserId,
                            "primary",
                            event
                    );

            Event googleEvent =
                    googleEventsService.getEventObject(
                            patientGoogleUserId,
                            "primary",
                            googleEventId
                    );

            eventInfo =
                    AppointmentMapper.toGoogleEventInfoResponse(
                            googleEvent
                    );
        } catch (Exception e) {
            log.warn("Could not create Meet link for patient {}: {}. Proceeding without Meet link.",
                    patientGoogleUserId, e.getMessage());
        }

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

        receipt.setCreatedBy(
                patientCaller.getId()
        );

        receipt =
                receiptRepository.save(
                        receipt
                );

        Appointment appointment =
                new Appointment();

        appointment.setCreatedBy(
                patientCaller.getId()
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

        appointment.setMeetLink(
                eventInfo != null ? eventInfo.meetLink() : null
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

    @Override
    public AppointmentTransactionResponse retryPaymentForExistingAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only PENDING_PAYMENT appointments can be retried");
        }

        PaymentIntentResponse paymentIntent = stripePaymentService.createPaymentIntent(
                PaymentIntentCreateRequest.builder()
                        .amount(appointment.getFinalFeePerHour())
                        .currency("usd")
                        .metadata(Map.of(
                                "appointment_id", appointment.getId().toString()
                        ))
                        .build()
        );

        GoogleEventInfoResponse eventInfo = new GoogleEventInfoResponse(appointment.getMeetLink());

        AppointmentResponse appointmentResponse = AppointmentMapper.toResponse(appointment, eventInfo);

        return new AppointmentTransactionResponse(
                appointmentResponse,
                paymentIntent.id(),
                paymentIntent.status(),
                paymentIntent.clientSecret(),
                paymentIntent.amount()
        );
    }

    @Override
    public CheckoutSessionResponse createCheckoutSession(UUID appointmentId, String successUrl, String cancelUrl) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only PENDING_PAYMENT appointments can initiate checkout");
        }

        if (!appStripeProperties.isEnabled()) {
            log.info("Stripe disabled - returning simulation URL for appointment {}", appointmentId);
            return new CheckoutSessionResponse(
                "/patient/simulate-payment/" + appointmentId,
                "simulation_session",
                appointmentId
            );
        }

        String checkoutUrl = stripePaymentService.createCheckoutSession(
                appointmentId,
                appointment.getFinalFeePerHour(),
                "usd",
                successUrl,
                cancelUrl
        );

        return new CheckoutSessionResponse(checkoutUrl, null, appointmentId);
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

        receipt.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        receipt.setUpdatedBy(
                appointment.getPatientCallerUser().getId()
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
    public AppointmentResponse confirmPayment(UUID appointmentId, String paymentIntentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only appointments with PENDING_PAYMENT status can be confirmed");
        }

        PaymentIntentResponse paymentIntent = stripePaymentService.getPaymentIntent(paymentIntentId);

        if (!"succeeded".equals(paymentIntent.status())) {
            throw new BusinessRuleException("Payment has not been completed successfully");
        }

        Receipt receipt = getReceipt(appointment);
        receipt.setPaymentStatus(PaymentStatus.PAID);
        receipt.setUpdatedBy(
                appointment.getPatientCallerUser().getId()
        );
        receiptRepository.save(receipt);

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        Appointment saved = appointmentRepository.save(appointment);

        return mapToResponseWithEventInfo(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmCheckoutSession(String sessionId) {
        log.info("=== confirmCheckoutSession called ===");
        log.info("sessionId: {}", sessionId);

        StripePaymentService.CheckoutSessionInfo sessionInfo = stripePaymentService.retrieveCheckoutSession(sessionId);
        log.info("Session retrieved - status: {}, paymentIntentId: {}, appointmentId: {}",
            sessionInfo.status(), sessionInfo.paymentIntentId(), sessionInfo.appointmentId());

        if (!"complete".equals(sessionInfo.status())) {
            log.warn("Checkout session not complete, status: {}", sessionInfo.status());
            throw new BusinessRuleException("Checkout session has not been completed");
        }

        if (sessionInfo.appointmentId() == null) {
            log.warn("No appointment_id in session metadata");
            throw new BusinessRuleException("No appointment linked to this checkout session");
        }

        Appointment appointment = appointmentRepository.findById(sessionInfo.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        log.info("Found appointment: {} with status: {}", appointment.getId(), appointment.getStatus());

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            log.warn("Appointment status is not PENDING_PAYMENT, current: {}", appointment.getStatus());
            throw new BusinessRuleException("Only appointments with PENDING_PAYMENT status can be confirmed");
        }

        PaymentIntentResponse paymentIntent = stripePaymentService.getPaymentIntent(sessionInfo.paymentIntentId());
        log.info("PaymentIntent retrieved - status: {}", paymentIntent.status());

        if (!"succeeded".equals(paymentIntent.status())) {
            log.warn("PaymentIntent status is not succeeded, current: {}", paymentIntent.status());
            throw new BusinessRuleException("Payment has not been completed successfully");
        }

        Receipt receipt = getReceipt(appointment);
        receipt.setPaymentStatus(PaymentStatus.PAID);
        receipt.setUpdatedBy(appointment.getPatientCallerUser().getId());
        receiptRepository.save(receipt);

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment {} confirmed and set to SCHEDULED", saved.getId());
        return mapToResponseWithEventInfo(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmSimulatePayment(UUID appointmentId) {
        if (appStripeProperties.isEnabled()) {
            throw new BusinessRuleException("Simulation not available when Stripe is enabled");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only PENDING_PAYMENT appointments can be confirmed");
        }

        Receipt receipt = getReceipt(appointment);
        receipt.setPaymentStatus(PaymentStatus.PAID);
        receipt.setUpdatedAt(java.time.OffsetDateTime.now());
        receiptRepository.save(receipt);

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setUpdatedAt(java.time.OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);

        log.info("SIMULATION: Appointment {} confirmed without Stripe", saved.getId());
        return mapToResponseWithEventInfo(saved);
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

    @Override
    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return mapToResponseWithEventInfo(appointment);
    }

    @Override
    @Transactional
    public void deleteAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only appointments with PENDING_PAYMENT status can be deleted");
        }

        if (appointment.getReceipt() != null) {
            receiptRepository.delete(appointment.getReceipt());
        }

        appointmentRepository.delete(appointment);
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

    private AppointmentResponse mapToResponseWithEventInfo(Appointment appointment) {
        GoogleEventInfoResponse eventInfo = new GoogleEventInfoResponse(appointment.getMeetLink());
        return AppointmentMapper.toResponse(appointment, eventInfo);
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
                            appointment.getMeetLink()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAppointmentsForDate(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return appointmentRepository.countByExpectedAtBetween(start, end);
    }
}
