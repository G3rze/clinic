package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {

    @Query("SELECT da FROM DoctorAvailability da WHERE da.employeeSpecialty.employeeId = :employeeId AND da.isActive = true")
    List<DoctorAvailability> findByEmployeeIdAndIsActiveTrue(@Param("employeeId") UUID employeeId);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.employeeSpecialty.employeeId = :employeeId AND da.dayOfWeek = :dayOfWeek AND da.isActive = true")
    List<DoctorAvailability> findByEmployeeIdAndDayOfWeekAndIsActiveTrue(
        @Param("employeeId") UUID employeeId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    @Query("SELECT da FROM DoctorAvailability da JOIN da.employeeSpecialty es WHERE es.specialty.code = :specialtyCode AND da.isActive = true")
    List<DoctorAvailability> findBySpecialtyCodeAndIsActiveTrue(@Param("specialtyCode") String specialtyCode);

    @Query("SELECT da FROM DoctorAvailability da JOIN FETCH da.employeeSpecialty es JOIN FETCH es.employee WHERE es.specialty.code = :specialtyCode AND da.isActive = true")
    List<DoctorAvailability> findBySpecialtyCodeWithEmployeeAndSpecialty(@Param("specialtyCode") String specialtyCode);

    @Query("SELECT da FROM DoctorAvailability da JOIN FETCH da.employeeSpecialty es JOIN FETCH es.employee WHERE da.id IN :ids")
    List<DoctorAvailability> findByIdsWithEmployeeAndSpecialty(@Param("ids") List<UUID> ids);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.employeeSpecialty.employeeId = :employeeId AND da.employeeSpecialty.specialtyId = :specialtyId AND da.isActive = true")
    List<DoctorAvailability> findByEmployeeIdAndSpecialtyIdAndIsActiveTrue(
        @Param("employeeId") UUID employeeId,
        @Param("specialtyId") UUID specialtyId
    );



}
