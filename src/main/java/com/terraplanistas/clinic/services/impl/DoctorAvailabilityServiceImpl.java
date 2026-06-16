package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.domain.entities.DoctorAvailability;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;
import com.terraplanistas.clinic.domain.mapper.DoctorAvailabilityMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.DoctorAvailabilityRepository;
import com.terraplanistas.clinic.repositories.EmployeeSpecialtyRepository;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;

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
}
