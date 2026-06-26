package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.AddAvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityService {

    AvailabilityResponse createAvailability(AvailabilityRequest request);

    AvailabilityResponse createAvailability(UUID employeeId, UUID specialtyId, AddAvailabilityRequest request, UUID createdBy);

    AvailabilityResponse updateAvailability(UUID id, AvailabilityRequest request);

    void deleteAvailability(UUID id);

    AvailabilityResponse getAvailability(UUID id);

    List<AvailabilityResponse> getAvailabilitiesByEmployeeAndSpecialty(UUID employeeId, UUID specialtyId);

    List<AvailabilityResponse> getAvailabilitiesByEmployee(UUID employeeId);
}
