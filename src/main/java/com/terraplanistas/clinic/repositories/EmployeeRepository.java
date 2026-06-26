package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u JOIN FETCH u.role r " +
           "WHERE e.isActive = true AND u.isAccessRevoked = false AND u.deletedAt IS NULL " +
           "AND r.code = 'EMPLOYEE' AND u.googleUserId IS NOT NULL")
    List<Employee> findActiveDoctors();

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u JOIN FETCH u.role r " +
           "WHERE e.isActive = true AND u.isAccessRevoked = false AND u.deletedAt IS NULL " +
           "AND r.code = 'EMPLOYEE' AND u.googleUserId IS NOT NULL")
    Page<Employee> findActiveDoctors(Pageable pageable);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u JOIN FETCH u.role r " +
           "WHERE e.isActive = true AND u.isAccessRevoked = false AND u.deletedAt IS NULL " +
           "AND r.code = 'EMPLOYEE' AND u.googleUserId IS NOT NULL " +
           "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Employee> findActiveDoctorsByName(@Param("name") String name);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u JOIN FETCH u.role r " +
           "WHERE e.isActive = true AND u.isAccessRevoked = false AND u.deletedAt IS NULL " +
           "AND r.code = 'EMPLOYEE' AND u.googleUserId IS NOT NULL " +
           "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Employee> findActiveDoctorsByName(@Param("name") String name, Pageable pageable);
}