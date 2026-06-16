package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.domain.entities.DoctorAvailability;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;

public class DoctorAvailabilityMapper {

    public static DoctorAvailability toEntity(AvailabilityRequest request, EmployeeSpecialty employeeSpecialty) {
        DoctorAvailability availability = new DoctorAvailability();
        availability.setEmployeeSpecialty(employeeSpecialty);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setIsActive(true);
        return availability;
    }

    public static void toUpgrade(AvailabilityRequest request, DoctorAvailability availability) {
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
    }

    public static AvailabilityResponse toResponse(DoctorAvailability availability) {
        return new AvailabilityResponse(
            availability.getId(),
            availability.getEmployeeSpecialty().getEmployeeId(),
            availability.getEmployeeSpecialty().getSpecialtyId(),
            availability.getDayOfWeek(),
            availability.getStartTime(),
            availability.getEndTime(),
            availability.getIsActive()
        );
    }
}
