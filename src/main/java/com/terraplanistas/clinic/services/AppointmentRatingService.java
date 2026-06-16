package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import java.util.UUID;

public interface AppointmentRatingService {

    AppointmentResponse rateAppointment(UUID appointmentId, UUID patientId, RateAppointmentRequest request);
}
