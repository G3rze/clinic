package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.EmployeeSpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.EmployeeSpecialtyResponse;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.Specialty;

public class EmployeeSpecialtyMapper {

    public static EmployeeSpecialty toEntity(EmployeeSpecialtyRequest request, Employee employee, Specialty specialty) {
        EmployeeSpecialty employeeSpecialty = new EmployeeSpecialty();
        employeeSpecialty.setEmployeeId(request.employeeId());
        employeeSpecialty.setSpecialtyId(request.specialtyId());
        employeeSpecialty.setEmployee(employee);
        employeeSpecialty.setSpecialty(specialty);
        employeeSpecialty.setProfessionalLicenseNumber(request.professionalLicenseNumber());
        employeeSpecialty.setFeePerHour(request.feePerHour());
        employeeSpecialty.setShift(request.shift());
        return employeeSpecialty;
    }

    public static EmployeeSpecialty toUpgrade(EmployeeSpecialtyRequest request, EmployeeSpecialty employeeSpecialty) {
        employeeSpecialty.setProfessionalLicenseNumber(request.professionalLicenseNumber());
        employeeSpecialty.setFeePerHour(request.feePerHour());
        employeeSpecialty.setShift(request.shift());
        return employeeSpecialty;
    }

    public static EmployeeSpecialtyResponse toResponse(EmployeeSpecialty employeeSpecialty) {
        return new EmployeeSpecialtyResponse(
            employeeSpecialty.getEmployeeId(),
            employeeSpecialty.getSpecialtyId(),
            employeeSpecialty.getProfessionalLicenseNumber(),
            employeeSpecialty.getFeePerHour(),
            employeeSpecialty.getShift(),
            null
        );
    }
}