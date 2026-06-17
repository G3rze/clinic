package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.CreateEmployeeRequest;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    User createEmployee(CreateEmployeeRequest request);

    List<Employee> getAllEmployees();

    List<Employee> getActiveEmployees();

    Employee getEmployeeById(UUID id);

    Employee getEmployeeByUserId(UUID userId);

    User getUserByEmployeeId(UUID employeeId);

    Employee updateEmployee(UUID id, CreateEmployeeRequest request);

    void revokeEmployeeAccess(UUID employeeId);

    void reactivateEmployee(UUID employeeId);
}
