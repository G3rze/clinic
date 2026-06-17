package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUserId(UUID userId);
    List<Employee> findByIsActiveTrue();
    Optional<Employee> findByUserIdAndIsActiveTrue(UUID userId);
    Optional<Employee> findByIdNumberBindex(String idNumberBindex);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u WHERE e.isActive = true " +
           "AND u.isAccessRevoked = false AND u.deletedAt IS NULL")
    List<Employee> findActiveWithNonRevokedUser();

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u WHERE u.id = :userId " +
           "AND u.isAccessRevoked = false AND u.deletedAt IS NULL")
    Optional<Employee> findByUserIdWithActiveUser(@Param("userId") UUID userId);
}