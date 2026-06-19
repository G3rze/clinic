package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentCancellationResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentTransactionResponse;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import java.time.YearMonth;
import java.util.UUID;

public interface AppointmentService {
    Page<AppointmentResponse> getAppointmentsForUser(UUID userId, YearMonth month, AppointmentStatus status);

    AppointmentTransactionResponse createAppointmentWithPayment(AppointmentTransactionRequest request);

    AppointmentResponse rateAppointment(UUID appointmentId, UUID patientId, RateAppointmentRequest request);

    AppointmentCancellationResponse cancelAppointment(
            UUID appointmentId

    );



}
