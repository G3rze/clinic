package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.mapper.AppointmentMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.services.AppointmentRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentRatingServiceImpl implements AppointmentRatingService {

    private final AppointmentRepository appointmentRepository;

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
        return AppointmentMapper.toResponse(saved);
    }
}
