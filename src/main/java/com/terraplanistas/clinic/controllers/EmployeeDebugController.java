package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.repositories.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/debug/employees")
public class EmployeeDebugController {

    private final EmployeeRepository employeeRepository;
    private final DataSource dataSource;

    public EmployeeDebugController(EmployeeRepository employeeRepository, DataSource dataSource) {
        this.employeeRepository = employeeRepository;
        this.dataSource = dataSource;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> debugEmployee(@PathVariable UUID id) {
        Map<String, Object> result = new HashMap<>();

        result.put("requestedId", id.toString());
        result.put("requestedIdBytes", java.util.Arrays.toString(id.toString().getBytes()));

        try (Connection conn = dataSource.getConnection()) {
            result.put("databaseUrl", conn.getMetaData().getURL());

            Optional<Employee> employeeOpt = employeeRepository.findById(id);
            result.put("employeeFoundViaHibernate", employeeOpt.isPresent());

            if (employeeOpt.isPresent()) {
                Employee emp = employeeOpt.get();
                result.put("employeeFirstName", emp.getFirstName());
                result.put("employeeIsActive", emp.getIsActive());
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}