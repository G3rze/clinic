package com.terraplanistas.clinic.domain.entities;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class DoctorAvailabilityId implements Serializable {
    private UUID employeeSpecialtyEmployeeId;
    private UUID employeeSpecialtySpecialtyId;

    public DoctorAvailabilityId() {}

    public DoctorAvailabilityId(UUID employeeSpecialtyEmployeeId, UUID employeeSpecialtySpecialtyId) {
        this.employeeSpecialtyEmployeeId = employeeSpecialtyEmployeeId;
        this.employeeSpecialtySpecialtyId = employeeSpecialtySpecialtyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoctorAvailabilityId that = (DoctorAvailabilityId) o;
        return employeeSpecialtyEmployeeId != null && employeeSpecialtyEmployeeId.equals(that.employeeSpecialtyEmployeeId)
            && employeeSpecialtySpecialtyId != null && employeeSpecialtySpecialtyId.equals(that.employeeSpecialtySpecialtyId);
    }

    @Override
    public int hashCode() {
        return 31 + (employeeSpecialtyEmployeeId != null ? employeeSpecialtyEmployeeId.hashCode() : 0)
            + (employeeSpecialtySpecialtyId != null ? employeeSpecialtySpecialtyId.hashCode() : 0);
    }
}
