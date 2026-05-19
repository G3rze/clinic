package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUserId(UUID userId);
    List<Employee> findByIsActiveTrue();
    Optional<Employee> findByUserIdAndIsActiveTrue(UUID userId);
    Optional<Employee> findByIdNumberBindex(String idNumberBindex);
}