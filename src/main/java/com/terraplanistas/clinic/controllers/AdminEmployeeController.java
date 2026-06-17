package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.CreateEmployeeRequest;
import com.terraplanistas.clinic.domain.dto.response.AdminEmployeeResponse;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.services.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<AdminEmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        User user = employeeService.createEmployee(request);
        Employee employee = employeeService.getEmployeeByUserId(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminEmployeeResponse.from(employee));
    }

    @GetMapping
    public ResponseEntity<List<AdminEmployeeResponse>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<AdminEmployeeResponse> response = employees.stream()
                .map(AdminEmployeeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminEmployeeResponse> getEmployeeById(@PathVariable UUID id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(AdminEmployeeResponse.from(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeResponse> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(AdminEmployeeResponse.from(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeEmployeeAccess(@PathVariable UUID id) {
        employeeService.revokeEmployeeAccess(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateEmployee(@PathVariable UUID id) {
        employeeService.reactivateEmployee(id);
        return ResponseEntity.ok().build();
    }
}
