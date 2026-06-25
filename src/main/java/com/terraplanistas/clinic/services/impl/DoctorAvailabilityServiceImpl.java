package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.AddAvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.domain.entities.DoctorAvailability;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialtyId;
import com.terraplanistas.clinic.domain.mapper.DoctorAvailabilityMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.DoctorAvailabilityRepository;
import com.terraplanistas.clinic.repositories.EmployeeSpecialtyRepository;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;

    @Value("${clinic.timezone}")
    private ZoneId clinicZone;

    @Override
    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request) {
        EmployeeSpecialty employeeSpecialty = employeeSpecialtyRepository
            .findById(new com.terraplanistas.clinic.domain.entities.EmployeeSpecialtyId(
                request.employeeId(), request.specialtyId()))
            .orElseThrow(() -> new ResourceNotFoundException(
                "EmployeeSpecialty not found for employee " + request.employeeId() + " and specialty " + request.specialtyId()));

        validateNoOverlap(request.employeeId(), request.specialtyId(),
            request.dayOfWeek(), request.startTime(), request.endTime(), null);

        DoctorAvailability availability = DoctorAvailabilityMapper.toEntity(request, employeeSpecialty);
        DoctorAvailability saved = availabilityRepository.save(availability);
        return DoctorAvailabilityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AvailabilityResponse createAvailability(UUID employeeId, UUID specialtyId,
            AddAvailabilityRequest request, UUID createdBy) {

        OffsetTime startTime = parseTime(request.startTime());
        OffsetTime endTime = parseTime(request.endTime());

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        long minutesBetween = Duration.between(startTime, endTime).toMinutes();
        if (minutesBetween < 30) {
            throw new IllegalArgumentException("Minimum slot duration is 30 minutes");
        }

        EmployeeSpecialtyId employeeSpecialtyId = new EmployeeSpecialtyId(employeeId, specialtyId);
        EmployeeSpecialty employeeSpecialty = employeeSpecialtyRepository.findById(employeeSpecialtyId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "EmployeeSpecialty not found for employee " + employeeId + " and specialty " + specialtyId));

        validateNoOverlap(employeeId, specialtyId, request.dayOfWeek(), startTime, endTime, null);

        DoctorAvailability availability = new DoctorAvailability();
        availability.setEmployeeSpecialty(employeeSpecialty);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setIsActive(true);
        availability.setCreatedBy(createdBy);

        DoctorAvailability saved = availabilityRepository.save(availability);
        return DoctorAvailabilityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AvailabilityResponse updateAvailability(UUID id, AvailabilityRequest request) {
        DoctorAvailability existing = availabilityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        validateNoOverlap(existing.getEmployeeSpecialty().getEmployeeId(),
            existing.getEmployeeSpecialty().getSpecialtyId(),
            request.dayOfWeek(), request.startTime(), request.endTime(), id);

        DoctorAvailabilityMapper.toUpgrade(request, existing);
        DoctorAvailability saved = availabilityRepository.save(existing);
        return DoctorAvailabilityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAvailability(UUID id) {
        DoctorAvailability availability = availabilityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));
        availability.setIsActive(false);
        availabilityRepository.save(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(UUID id) {
        DoctorAvailability availability = availabilityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));
        return DoctorAvailabilityMapper.toResponse(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilitiesByEmployeeAndSpecialty(UUID employeeId, UUID specialtyId) {
        List<DoctorAvailability> availabilities = availabilityRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
        return availabilities.stream()
            .filter(a -> a.getEmployeeSpecialty().getSpecialtyId().equals(specialtyId))
            .map(DoctorAvailabilityMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilitiesByEmployee(UUID employeeId) {
        List<DoctorAvailability> availabilities = availabilityRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
        return availabilities.stream()
            .map(DoctorAvailabilityMapper::toResponse)
            .toList();
    }

    private void validateNoOverlap(UUID employeeId, UUID specialtyId, DayOfWeek dayOfWeek,
            java.time.OffsetTime startTime, java.time.OffsetTime endTime, UUID excludeId) {
        List<DoctorAvailability> existingSlots = availabilityRepository
            .findByEmployeeIdAndDayOfWeekAndIsActiveTrue(employeeId, dayOfWeek);

        for (DoctorAvailability slot : existingSlots) {
            if (excludeId != null && slot.getId().equals(excludeId)) {
                continue;
            }

            boolean overlaps = !startTime.isAfter(slot.getEndTime()) && !endTime.isBefore(slot.getStartTime());
            if (overlaps) {
                throw new BusinessRuleException(String.format(
                    "Availability slot overlaps with existing slot [%s - %s] on %s",
                    slot.getStartTime(), slot.getEndTime(), dayOfWeek));
            }
        }
    }

    private OffsetTime parseTime(String time) {
        LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        // Parse input time as clinic timezone, then convert to UTC for storage
        OffsetTime clinicTime = localTime.atOffset(clinicZone.getRules().getOffset(Instant.now()));
        return clinicTime.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
