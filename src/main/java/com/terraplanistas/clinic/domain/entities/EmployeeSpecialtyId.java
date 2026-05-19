package com.terraplanistas.clinic.domain.entities;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class EmployeeSpecialtyId implements Serializable {
    private UUID employeeId;
    private UUID specialtyId;

    public EmployeeSpecialtyId() {}

    public EmployeeSpecialtyId(UUID employeeId, UUID specialtyId) {
        this.employeeId = employeeId;
        this.specialtyId = specialtyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeSpecialtyId that = (EmployeeSpecialtyId) o;
        return employeeId != null && employeeId.equals(that.employeeId)
            && specialtyId != null && specialtyId.equals(that.specialtyId);
    }

    @Override
    public int hashCode() {
        return 31 + (employeeId != null ? employeeId.hashCode() : 0)
            + (specialtyId != null ? specialtyId.hashCode() : 0);
    }
}