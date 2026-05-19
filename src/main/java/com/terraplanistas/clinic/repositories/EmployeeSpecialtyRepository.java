package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.EmployeeSpecialty;
import com.terraplanistas.clinic.domain.entities.EmployeeSpecialtyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSpecialtyRepository extends JpaRepository<EmployeeSpecialty, EmployeeSpecialtyId> {
    List<EmployeeSpecialty> findByEmployeeId(UUID employeeId);
    List<EmployeeSpecialty> findBySpecialtyId(UUID specialtyId);
    @Query("SELECT es FROM EmployeeSpecialty es JOIN FETCH es.specialty WHERE es.employeeId = :employeeId")
    List<EmployeeSpecialty> findByEmployeeIdWithSpecialty(@Param("employeeId") UUID employeeId);
    @Query("SELECT es FROM EmployeeSpecialty es JOIN FETCH es.employee WHERE es.specialtyId = :specialtyId")
    List<EmployeeSpecialty> findBySpecialtyIdWithEmployee(@Param("specialtyId") UUID specialtyId);
    Optional<EmployeeSpecialty> findByProfessionalLicenseNumber(String professionalLicenseNumber);
}