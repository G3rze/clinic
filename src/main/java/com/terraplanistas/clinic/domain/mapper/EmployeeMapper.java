package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.EmployeeRequest;
import com.terraplanistas.clinic.domain.dto.response.EmployeeResponse;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;

public class EmployeeMapper {

    public static Employee toEntity(EmployeeRequest request, User user) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setIdNumber(request.idNumber());
        employee.setIdType(request.idType());
        employee.setAddress(request.address());
        employee.setPhones(request.phones());
        employee.setIsActive(true);
        return employee;
    }

    public static Employee toUpgrade(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setIdNumber(request.idNumber());
        employee.setIdType(request.idType());
        employee.setAddress(request.address());
        employee.setPhones(request.phones());
        return employee;
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getIdNumber(),
            employee.getIdType(),
            employee.getAddress(),
            employee.getPhones(),
            employee.getIsActive(),
            employee.getUser() != null ? employee.getUser().getId() : null
        );
    }
}